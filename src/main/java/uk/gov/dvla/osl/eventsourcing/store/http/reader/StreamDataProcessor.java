package uk.gov.dvla.osl.eventsourcing.store.http.reader;

import uk.gov.dvla.osl.eventsourcing.api.DataFetcher;
import uk.gov.dvla.osl.eventsourcing.api.DataProcessor;
import uk.gov.dvla.osl.eventsourcing.api.EntryProcessor;
import uk.gov.dvla.osl.eventsourcing.api.LinkProcessor;
import uk.gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import uk.gov.dvla.osl.eventsourcing.store.http.entity.EventStreamData;
import uk.gov.dvla.osl.eventsourcing.store.http.entity.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

import java.io.IOException;
import java.util.List;

public class StreamDataProcessor implements DataProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamDataProcessor.class);
    private static final String HEAD_URL = "streams/%s/%d/forward/%d";
    private static final boolean DO_LONGPOLL = true;
    private static final boolean DONT_LONGPOLL = false;

    private final EntryProcessor entryProcessor;
    private final LinkProcessor linkProcessor;
    private final DataFetcher dataFetcher;
    private final EventStoreConfiguration configuration;
    private boolean keepGoing = true;

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
                            final int nextVersionNumber) throws IOException {

        final String headUrl = String.format(HEAD_URL, streamName, nextVersionNumber, configuration.getProjectionConfiguration().getPageSize());

        List<Link> links = getAndProcessEntries(subscriber, headUrl, DONT_LONGPOLL);

        String lastLinkProcessed = headUrl;
        String previous;

        do {
            while (linkProcessor.getUriByRelation(links, "previous").equals("")) {
                if (keepAlive) {
                    links = getAndProcessEntries(subscriber, lastLinkProcessed, DO_LONGPOLL);
                } else {
                    subscriber.onCompleted();
                    return;
                }
            }

            previous = linkProcessor.getUriByRelation(links, "previous");

            links = getAndProcessEntries(subscriber, previous, DONT_LONGPOLL);

            lastLinkProcessed = previous;

        } while (this.keepGoing);
    }

    private List<Link> getAndProcessEntries(final Subscriber subscriber, final String headUrl, final boolean longPoll) throws IOException {
        EventStreamData eventStreamData = dataFetcher.fetchStreamData(headUrl, longPoll);
        entryProcessor.provideEntriesToSubscriber(eventStreamData.getEntries(), subscriber);
        return eventStreamData.getLinks();
    }

    @Override
    public void shutDown() {
        this.keepGoing = false;
    }
}
