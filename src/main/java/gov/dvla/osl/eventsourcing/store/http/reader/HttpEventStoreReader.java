package gov.dvla.osl.eventsourcing.store.http.reader;

import gov.dvla.osl.eventsourcing.api.*;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.impl.DefaultEventDeserialiser;
import gov.dvla.osl.eventsourcing.store.http.*;
import gov.dvla.osl.eventsourcing.store.http.entity.Entry;
import gov.dvla.osl.eventsourcing.store.http.entity.EventStreamData;
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

    private EventStoreConfiguration configuration;
    private int nextVersionNumber;
    private EntryProcessor entryProcessor;
    private LinkProcessor linkProcessor;
    private DataFetcher dataFetcher;
    private EventDeserialiser eventDeserialiser;
    private boolean keepGoing = true;

    public HttpEventStoreReader(EventStoreConfiguration configuration,
                                EntryProcessor entryProcessor,
                                LinkProcessor linkProcessor,
                                DataFetcher dataFetcher,
                                EventDeserialiser eventDeserialiser) throws IOException {
        this.configuration = configuration;
        this.entryProcessor = entryProcessor;
        this.linkProcessor = linkProcessor;
        this.dataFetcher = dataFetcher;
        this.eventDeserialiser = eventDeserialiser;
    }

    public HttpEventStoreReader(EventStoreConfiguration configuration) throws IOException {
        this(configuration,
                new StreamEntryProcessor(),
                new StreamLinkProcessor(),
                new StreamDataFetcher(ServiceGenerator.createService(EventStoreService.class, configuration),
                        configuration.getProjectionConfiguration().getLongPollSeconds()),
                new DefaultEventDeserialiser());
    }

    @Override
    public EventStream loadEventStream(String streamName) {

        final long[] lastEventNumber = {0};
        List<Event> events = new ArrayList<>();

        Observable.create(new Observable.OnSubscribe<Entry>() {
            @Override
            public void call(Subscriber<? super Entry> subscriber) {
                try {
                    processData(subscriber, streamName, false);
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
                () -> LOGGER.debug("Dealer projection finished")
        );

        if (events.size() == 0)
            return new ListEventStream(-1, events);
        else
            return new ListEventStream(lastEventNumber[0], events);
    }

    @Override
    public Observable<EventStoreEvent> all() {
        return null;
    }

    @Override
    public Observable<EventStoreEvent> streamFrom(String streamName) {
        return null;
    }

    public Observable<Entry> readStreamEventsForward(Func0<Integer> getNextVersionNumber) {
        nextVersionNumber = getNextVersionNumber.call();
        return Observable.create(new Observable.OnSubscribe<Entry>() {
            @Override
            public void call(Subscriber<? super Entry> subscriber) {
                try {
                    processData(subscriber,
                            configuration.getProjectionConfiguration().getStream(),
                            configuration.getProjectionConfiguration().isKeepAlive());
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });
    }

    public void shutdown() {
        keepGoing = false;
    }

    private void processData(Subscriber subscriber, String streamName, boolean keepAlive) throws Exception {

        String headUrl = String.format("streams/%s/%d/forward/%d", streamName, nextVersionNumber, configuration.getProjectionConfiguration().getPageSize());

        EventStreamData eventStreamData = dataFetcher.getStreamData(headUrl, false);

        entryProcessor.provideEntriesToSubscriber(eventStreamData.getEntries(), subscriber);

        String lastLinkProcessed = headUrl;
        String previous;

        do {
            while (linkProcessor.getUriByRelation(eventStreamData.getLinks(), "previous").equals("")) {

                if (configuration.getProjectionConfiguration().isKeepAlive()) {
                    eventStreamData = dataFetcher.getStreamData(lastLinkProcessed, true);
                    entryProcessor.provideEntriesToSubscriber(eventStreamData.getEntries(), subscriber);
                } else {
                    subscriber.onCompleted();
                    return;
                }
            }

            previous = linkProcessor.getUriByRelation(eventStreamData.getLinks(), "previous");

            eventStreamData = dataFetcher.getStreamData(previous, false);
            entryProcessor.provideEntriesToSubscriber(eventStreamData.getEntries(), subscriber);

            lastLinkProcessed = previous;

        } while (keepGoing);
    }
}

