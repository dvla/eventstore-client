package gov.dvla.osl.eventsourcing.store.http.reader;

import gov.dvla.osl.eventsourcing.api.DataFetcher;
import gov.dvla.osl.eventsourcing.api.DataProcessor;
import gov.dvla.osl.eventsourcing.api.EntryProcessor;
import gov.dvla.osl.eventsourcing.api.LinkProcessor;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.store.http.entity.EventStreamData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

public class StreamDataProcessor implements DataProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamDataProcessor.class);
    private static final String HEAD_URL = "streams/%s/%d/forward/%d";
    private static final boolean DO_LONGPOLL = true;
    private static final boolean DONT_LONGPOLL = false;

    private final EntryProcessor entryProcessor;
    private final LinkProcessor linkProcessor;
    private final DataFetcher dataFetcher;
    private final EventStoreConfiguration configuration;
    private boolean keepGoing;

    public StreamDataProcessor(final EntryProcessor entryProcessor,
                               final LinkProcessor linkProcessor,
                               final DataFetcher dataFetcher,
                               final EventStoreConfiguration configuration) {

        this.entryProcessor = entryProcessor;
        this.linkProcessor = linkProcessor;
        this.dataFetcher = dataFetcher;
        this.configuration = configuration;
    }

    public void processData(final Subscriber subscriber,
                            final String streamName,
                            final boolean keepAlive,
                            int nextVersionNumber) throws Exception {

        String headUrl = String.format(HEAD_URL, streamName, nextVersionNumber, configuration.getProjectionConfiguration().getPageSize());

        EventStreamData eventStreamData = dataFetcher.getStreamData(headUrl, DONT_LONGPOLL);
        entryProcessor.provideEntriesToSubscriber(eventStreamData.getEntries(), subscriber);

        String lastLinkProcessed = headUrl;
        String previous;

        do {
            while (linkProcessor.getUriByRelation(eventStreamData.getLinks(), "previous").equals("")) {

                if (keepAlive) {
                    eventStreamData = dataFetcher.getStreamData(lastLinkProcessed, DO_LONGPOLL);
                    entryProcessor.provideEntriesToSubscriber(eventStreamData.getEntries(), subscriber);
                } else {
                    subscriber.onCompleted();
                    return;
                }
            }

            previous = linkProcessor.getUriByRelation(eventStreamData.getLinks(), "previous");

            eventStreamData = dataFetcher.getStreamData(previous, DONT_LONGPOLL);
            entryProcessor.provideEntriesToSubscriber(eventStreamData.getEntries(), subscriber);

            lastLinkProcessed = previous;

        } while (this.keepGoing);
    }

    @Override
    public void shutDown() {
        this.keepGoing = false;
    }
}
