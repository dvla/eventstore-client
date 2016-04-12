package gov.dvla.osl.eventsourcing.store.httpeventstore;

import gov.dvla.osl.eventsourcing.api.DataFetcher;
import gov.dvla.osl.eventsourcing.api.EntryProcessor;
import gov.dvla.osl.eventsourcing.api.LinkProcessor;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.Entry;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.EventStreamData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

import java.io.IOException;

public class EventStoreStream {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventStoreStream.class);

    private EventStoreConfiguration configuration;
    private int nextVersionNumber;
    private EntryProcessor entryProcessor;
    private LinkProcessor linkProcessor;
    private DataFetcher dataFetcher;
    private boolean keepGoing = true;

    public EventStoreStream(EventStoreConfiguration configuration,
                            EntryProcessor entryProcessor,
                            LinkProcessor linkProcessor,
                            DataFetcher dataFetcher) throws IOException {
        this.configuration = configuration;
        this.entryProcessor = entryProcessor;
        this.linkProcessor = linkProcessor;
        this.dataFetcher = dataFetcher;
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

