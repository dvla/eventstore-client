package gov.dvla.osl.eventsourcing.store.httpeventstore;

import gov.dvla.osl.eventsourcing.api.*;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.Entry;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.EventStreamData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

import java.io.IOException;

public class HttpEventStoreReader implements EventStoreReader<Long> {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpEventStoreReader.class);

    private EventStoreConfiguration configuration;
    private int nextVersionNumber;
    private EntryProcessor entryProcessor;
    private LinkProcessor linkProcessor;
    private DataFetcher dataFetcher;
    private boolean keepGoing = true;

    public HttpEventStoreReader(EventStoreConfiguration configuration,
                                EntryProcessor entryProcessor,
                                LinkProcessor linkProcessor,
                                DataFetcher dataFetcher) throws IOException {
        this.configuration = configuration;
        this.entryProcessor = entryProcessor;
        this.linkProcessor = linkProcessor;
        this.dataFetcher = dataFetcher;
    }

    @Override
    public EventStream<Long> loadEventStream(String aggregateId) {
        return null;
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
        return Observable.create(subscribeFunction);
    }

    public void shutdown() {
        keepGoing = false;
    }

    Observable.OnSubscribe<Entry> subscribeFunction = (sub) -> {

        Subscriber subscriber = (Subscriber) sub;

        try {
            processData(subscriber);
        } catch (Exception e) {
            subscriber.onError(e);
        }
    };

    private void processData(Subscriber subscriber) throws Exception {

        String headUrl = String.format("streams/%s/%d/forward/%d", configuration.getProjectionConfiguration().getStream(), nextVersionNumber, configuration.getProjectionConfiguration().getPageSize());

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

