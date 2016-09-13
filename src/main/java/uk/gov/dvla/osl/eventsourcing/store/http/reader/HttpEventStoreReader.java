package uk.gov.dvla.osl.eventsourcing.store.http.reader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import uk.gov.dvla.osl.eventsourcing.api.DataProcessor;
import uk.gov.dvla.osl.eventsourcing.api.Event;
import uk.gov.dvla.osl.eventsourcing.api.EventDeserialiser;
import uk.gov.dvla.osl.eventsourcing.api.EventStoreReader;
import uk.gov.dvla.osl.eventsourcing.api.EventStream;
import uk.gov.dvla.osl.eventsourcing.api.ReadDirection;
import uk.gov.dvla.osl.eventsourcing.api.StreamPosition;
import uk.gov.dvla.osl.eventsourcing.api.Take;
import uk.gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import uk.gov.dvla.osl.eventsourcing.exception.EventStoreClientTechnicalException;
import uk.gov.dvla.osl.eventsourcing.impl.DefaultEventDeserialiser;
import uk.gov.dvla.osl.eventsourcing.store.http.EventStoreService;
import uk.gov.dvla.osl.eventsourcing.store.http.ServiceGenerator;
import uk.gov.dvla.osl.eventsourcing.store.http.entity.Entry;
import uk.gov.dvla.osl.eventsourcing.api.ListEventStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class HttpEventStoreReader implements EventStoreReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpEventStoreReader.class);

    private final EventStoreConfiguration configuration;

    private final DataProcessor dataProcessor;
    private final EventDeserialiser eventDeserialiser;

    public HttpEventStoreReader(final EventStoreConfiguration configuration,
                                final DataProcessor dataProcessor,
                                final EventDeserialiser eventDeserialiser) throws IOException {
        this.configuration = configuration;
        this.dataProcessor = dataProcessor;
        this.eventDeserialiser = eventDeserialiser;
    }

    public HttpEventStoreReader(final EventStoreConfiguration configuration) throws IOException {
        this(configuration,
                new StreamDataProcessor(new StreamEntryProcessor(),
                        new StreamLinkProcessor(),
                        new StreamDataFetcher(ServiceGenerator.createService(EventStoreService.class, configuration),
                                configuration.getProjectionConfiguration().getLongPollSeconds())),
                new DefaultEventDeserialiser());
    }

    @Override
    public EventStream loadEventStream(final String streamName) {
        return loadEventStream(streamName, StreamPosition.START);
    }

    @Override
    public EventStream loadEventStream(String streamName, int start) {

        final long[] lastEventNumber = {0};
        final List<Event> events = new ArrayList<>();

        readStreamEventsForward(streamName,
                start,
                configuration.getProjectionConfiguration().getPageSize(),
                false)
                .retryWhen(errors -> errors.flatMap(error -> {
                    LOGGER.error("An error occurred processing the stream {}", error.getMessage());
                    return Observable.timer(configuration.getProjectionConfiguration().getSecondsBeforeRetry(), TimeUnit.SECONDS);
                })).subscribe(
                (entry) -> {
                    lastEventNumber[0] = entry.getEventNumber();
                    Event event = this.eventDeserialiser.deserialise(entry.getData(), entry.getEventType());
                    events.add(event);
                },
                (error) -> LOGGER.error(error.getMessage(), error),
                () -> LOGGER.debug("Projection finished")
        );

        if (events.size() == 0) {
            LOGGER.debug(String.format("HttpEventStoreReader.loadEventStream(streamName=%s, start=%s).  No events returned", streamName, start));
            return new ListEventStream(-1, events);
        }
        else {
            LOGGER.debug(String.format("HttpEventStoreReader.loadEventStream(streamName=%s, start=%s).  %d events returned", streamName, start, events.size()));
            return new ListEventStream(lastEventNumber[0], events);
        }
    }

    @Override
    public EventStream loadEventStreamWithLastEvent(final String streamName) {

        final long[] lastEventNumber = {0};
        final List<Event> events = new ArrayList<>();
        final List<Entry> entries = new ArrayList<>();

        readStreamEventsBackward(streamName,
                StreamPosition.END, 1, false).subscribe(
                (Entry entry) -> {
                    lastEventNumber[0] = entry.getEventNumber();
                    Event event = this.eventDeserialiser.deserialise(entry.getData(), entry.getEventType());
                    events.add(event);
                    entries.add(entry);
                },
                (error) -> LOGGER.error(error.getMessage(), error),
                () -> LOGGER.debug("Projection finished")
        );

        final List<Event> eventsToReturn = events.stream().limit(1).collect(Collectors.toList());

        return new ListEventStream(eventsToReturn.size() == 0 ? -1 : entries.stream().findFirst().get().getPositionEventNumber(), eventsToReturn);
    }

    @Override
    public Observable<Entry> readStreamEventsForward(final String streamName,
                                                     final int start,
                                                     final int count,
                                                     final boolean keepAlive) {
        return Observable.create(subscriber -> {
            try {
                dataProcessor.processData(subscriber,
                        streamName,
                        keepAlive,
                        start,
                        count,
                        Take.ALL,
                        ReadDirection.FORWARD);
            } catch (EventStoreClientTechnicalException e) {
                if (e.getMessage().contains("404"))
                    subscriber.onCompleted();
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    @Override
    public Observable<Entry> readStreamEventsBackward(final String streamName,
                                                      final int start,
                                                      final int count,
                                                      final boolean keepAlive) {
        return Observable.create(subscriber -> {
            try {
                dataProcessor.processData(subscriber,
                        streamName,
                        keepAlive,
                        start,
                        count,
                        Take.ALL,
                        ReadDirection.BACKWARD);
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    @Override
    public Observable<Entry> readLastEvent(String streamName) {
        return Observable.create(subscriber -> {
            try {
                dataProcessor.processData(subscriber,
                        streamName,
                        false,
                        StreamPosition.END,
                        1,
                        Take.ONE,
                        ReadDirection.BACKWARD);
            } catch (Exception e) {
                subscriber.onError(e);
            }
        });
    }

    public void shutdown() {
        dataProcessor.shutDown();
    }
}

