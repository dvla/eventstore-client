package gov.dvla.osl.eventsourcing.store.httpeventstore;

import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.Entry;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.EventStreamData;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.Link;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;
import rx.Observable;
import rx.Subscriber;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class EventStoreStream {

    private static final Logger logger = LoggerFactory.getLogger(EventStoreStream.class);

    private EventStoreConfiguration configuration;
    private int nextVersionNumber;
    private EventStoreService service;
    private boolean keepGoing = true;

    public EventStoreStream(EventStoreService service, EventStoreConfiguration configuration, int nextVersionNumber) throws IOException {
        this.configuration = configuration;
        this.nextVersionNumber = nextVersionNumber;
        this.service = service;
    }

    public Observable<Entry> readStreamEventsForward() {
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

        EventStreamData eventStreamData = getUrl(String.format("streams/%s/%d/forward/%d", configuration.getStream(), nextVersionNumber, configuration.getPageSize()), false);

        String previous;

        do {
            for (int i = eventStreamData.getEntries().size() - 1; i > -1; i--) {
                logger.debug("Calling subscriber.onNext with " + eventStreamData.getEntries().get(i).getEventType());
                subscriber.onNext(eventStreamData.getEntries().get(i));
            }

            previous = getPreviousLink(eventStreamData.getLinks());

            eventStreamData = getUrl(previous, false);

            while (eventStreamData.getEntries().size() == 0 && keepGoing) {
                if (configuration.isKeepAlive()) {
                    eventStreamData = getUrl(previous, true);
                } else {
                    subscriber.onCompleted();
                    return;
                }
            }
        } while (keepGoing);
    }

    private String getPreviousLink(List<Link> links) {

        for (int i = 0; i < links.size() - 1; i++) {
            if (links.get(i).getRelation().equals("previous"))
                return links.get(i).getUri();
        }

        return "";
    }

    private EventStreamData getUrl(String url, boolean longPoll) {

        Call<EventStreamData> eventStream = service.getEventStreamData(longPoll ? "10" : null, url + "?embed=body");

        Response<EventStreamData> response = null;

        try {
            response = eventStream.execute();
            if (response.isSuccess()) return response.body();
        } catch (ConnectException e) {
            logger.error(e.getMessage());
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
}