package gov.dvla.osl.eventsourcing.store.httpeventstore;

import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.exception.EventStoreClientTechnicalException;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.Entry;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.EventStreamData;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

import java.io.IOException;
import java.util.List;

public class EventStoreStream {

    private static final Logger logger = LoggerFactory.getLogger(EventStoreStream.class);

    private EventStoreConfiguration configuration;
    private int nextVersionNumber;
    private EventStoreService service;
    private boolean keepGoing = true;

    public EventStoreStream(EventStoreService service, EventStoreConfiguration configuration) throws IOException {
        this.configuration = configuration;
        this.service = service;
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

        EventStreamData eventStreamData = getUrl(headUrl, false);

        processEntries(eventStreamData.getEntries(), subscriber);

        String lastLinkProcessed = headUrl;
        String previous;

        do {
            while (getPreviousLink(eventStreamData.getLinks()).equals("")) {

                if (configuration.getProjectionConfiguration().isKeepAlive()) {
                    eventStreamData = getUrl(lastLinkProcessed, true);
                    processEntries(eventStreamData.getEntries(), subscriber);
                } else {
                    subscriber.onCompleted();
                    return;
                }
            }

            previous = getPreviousLink(eventStreamData.getLinks());

            eventStreamData = getUrl(previous, false);
            processEntries(eventStreamData.getEntries(), subscriber);

            lastLinkProcessed = previous;

        } while (keepGoing);
    }

    private void processEntries(List<Entry> entries, Subscriber subscriber) {
        for (int i = entries.size() - 1; i > -1; i--) {
            logger.debug("Calling subscriber.onNext with " + entries.get(i).getEventType());
            subscriber.onNext(entries.get(i));
        }
    }

    private String getPreviousLink(List<Link> links) {

        for (int i = 0; i < links.size() - 1; i++) {
            if (links.get(i).getRelation().equals("previous"))
                return links.get(i).getUri();
        }

        return "";
    }

    private EventStreamData getUrl(String url, boolean longPoll) throws IOException {

        if (longPoll)
            logger.info("Starting long-poll with value of " + configuration.getProjectionConfiguration().getLongPollSeconds());

        Call<EventStreamData> eventStream = service.getEventStreamData(longPoll ? configuration.getProjectionConfiguration().getLongPollSeconds() : null, url + "?embed=body");

        Response<EventStreamData> response = eventStream.execute();
        if (response.isSuccess())
            return response.body();
        else
            throw new EventStoreClientTechnicalException(String.format("GET failed on %s with status %d", url, response.code()));
    }
}