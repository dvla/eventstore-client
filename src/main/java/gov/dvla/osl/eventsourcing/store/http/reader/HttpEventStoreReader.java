package gov.dvla.osl.eventsourcing.store.http.reader;

import gov.dvla.osl.eventsourcing.api.*;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.exception.EventStoreClientTechnicalException;
import gov.dvla.osl.eventsourcing.impl.DefaultEventDeserialiser;
import gov.dvla.osl.eventsourcing.store.http.*;
import gov.dvla.osl.eventsourcing.store.http.entity.Entry;
import gov.dvla.osl.eventsourcing.store.memory.ListEventStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class HttpEventStoreReader implements EventStoreReader<Long> {

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
                            configuration.getProjectionConfiguration().getLongPollSeconds()),
                        configuration),
                new DefaultEventDeserialiser());
    }

    @Override
    public EventStream loadEventStream(final String streamName) {

        final long[] lastEventNumber = {0};
        final List<Event> events = new ArrayList<>();

        Observable.create(new Observable.OnSubscribe<Entry>() {
            @Override
            public void call(Subscriber<? super Entry> subscriber) {
                try {
                    dataProcessor.processData(subscriber, streamName, false, 0);
                } catch (EventStoreClientTechnicalException e) {
                    if (e.getMessage().contains("404"))
                        subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        }).retryWhen(errors -> {
            return errors.flatMap(error -> {
                if (error.hasThrowable())
                    LOGGER.error("An error occurred processing the stream", error.getThrowable());
                return Observable.timer(configuration.getProjectionConfiguration().getSecondsBeforeRetry(), TimeUnit.SECONDS);
            });
        }).subscribe(
                (entry) -> {
                    lastEventNumber[0] = entry.getEventNumber();
                    Event event = this.eventDeserialiser.deserialise(entry.getData(), entry.getEventType());
                    events.add(event);
                },
                (error) -> LOGGER.error(error.getMessage(), error),
                () -> LOGGER.debug("Projection finished")
        );

        if (events.size() == 0)
            return new ListEventStream(-1, events);
        else
            return new ListEventStream(lastEventNumber[0], events);
    }

    public Observable<Entry> readStreamEventsForward(Func0<Integer> getNextVersionNumber) {
        return Observable.create(new Observable.OnSubscribe<Entry>() {
            @Override
            public void call(Subscriber<? super Entry> subscriber) {
                try {
                    dataProcessor.processData(subscriber,
                            configuration.getProjectionConfiguration().getStream(),
                            configuration.getProjectionConfiguration().isKeepAlive(),
                            getNextVersionNumber.call());
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public void shutdown() {
        dataProcessor.shutDown();
    }
}

