package uk.gov.dvla.osl.eventsourcing.store.http.reader;

import uk.gov.dvla.osl.eventsourcing.api.DataFetcher;
import uk.gov.dvla.osl.eventsourcing.api.DataProcessor;
import uk.gov.dvla.osl.eventsourcing.api.EntryProcessor;
import uk.gov.dvla.osl.eventsourcing.api.LinkProcessor;
import uk.gov.dvla.osl.eventsourcing.api.ReadDirection;
import uk.gov.dvla.osl.eventsourcing.api.StreamPosition;
import uk.gov.dvla.osl.eventsourcing.api.Take;
import uk.gov.dvla.osl.eventsourcing.store.http.entity.EventStreamData;
import uk.gov.dvla.osl.eventsourcing.store.http.entity.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

import java.io.IOException;
import java.util.List;

public class StreamDataProcessor implements DataProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamDataProcessor.class);
    private static final String URL = "streams/%s/%s/%s/%d";
    private static final boolean DO_LONGPOLL = true;
    private static final boolean DONT_LONGPOLL = false;

    private final EntryProcessor entryProcessor;
    private final LinkProcessor linkProcessor;
    private final DataFetcher dataFetcher;
    private boolean keepGoing = true;

    public StreamDataProcessor(final EntryProcessor entryProcessor,
                               final LinkProcessor linkProcessor,
                               final DataFetcher dataFetcher) {
        this.entryProcessor = entryProcessor;
        this.linkProcessor = linkProcessor;
        this.dataFetcher = dataFetcher;
    }

    public void processData(final Subscriber subscriber,
                            final String streamName,
                            final boolean keepAlive,
                            final int start,
                            final int pageSize,
                            final Take take,
                            final ReadDirection readDirection) throws IOException {

        final String headUrl = String.format(URL,
                streamName,
                start == StreamPosition.END ? "head" : start,
                readDirection == ReadDirection.FORWARD ? "forward" : "backward",
                pageSize);

        final String linkName = readDirection == ReadDirection.FORWARD ? "previous" : "next";

        List<Link> links = getAndProcessEntries(subscriber, headUrl, DONT_LONGPOLL, take, readDirection);

        if (take == Take.ONE) {
            subscriber.onCompleted();
            return;
        }

        String lastLinkProcessed = headUrl;
        String linkUri;

        do {
            while (linkProcessor.getUriByRelation(links, linkName).equals("")) {
                if (keepAlive) {
                    links = getAndProcessEntries(subscriber, lastLinkProcessed, DO_LONGPOLL, take, readDirection);
                } else {
                    subscriber.onCompleted();
                    return;
                }
            }

            linkUri = linkProcessor.getUriByRelation(links, linkName);

            links = getAndProcessEntries(subscriber, linkUri, DONT_LONGPOLL, take, readDirection);

            lastLinkProcessed = linkUri;

        } while (this.keepGoing);
    }

    private List<Link> getAndProcessEntries(final Subscriber subscriber,
                                            final String headUrl,
                                            final boolean longPoll,
                                            final Take take,
                                            final ReadDirection readDirection) throws IOException {
        LOGGER.debug("StreamDataProcessor.getAndProcessEntries: headUrl=" + headUrl);
        EventStreamData eventStreamData = dataFetcher.fetchStreamData(headUrl, longPoll);
        entryProcessor.provideEntriesToSubscriber(eventStreamData.getEntries(), subscriber, take, readDirection);
        return eventStreamData.getLinks();
    }

    @Override
    public void shutDown() {
        this.keepGoing = false;
    }
}
