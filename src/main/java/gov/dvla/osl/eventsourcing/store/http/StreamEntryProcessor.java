package gov.dvla.osl.eventsourcing.store.http;

import gov.dvla.osl.eventsourcing.api.EntryProcessor;
import gov.dvla.osl.eventsourcing.store.http.entity.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Subscriber;

import java.util.List;

public class StreamEntryProcessor implements EntryProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(StreamEntryProcessor.class);

    /**
     * If an event stream is hard deleted then the event type is labelled "$metadata".  Ensure
     * these events are not processed.
     */
    private static final String HARD_DELETED_EVENT_TYPE = "$metadata";

    public void provideEntriesToSubscriber(List<Entry> entries, Subscriber subscriber) {

        for (int i = entries.size() - 1; i > -1; i--) {
            Entry entry = entries.get(i);
            if (entry != null && entry.getEventNumber() != null && entry.getEventType() != null && !entry.getEventType().equals(HARD_DELETED_EVENT_TYPE)) {
                LOGGER.debug("Calling subscriber.onNext with " + entries.get(i).getEventType());
                subscriber.onNext(entry);
            }
        }
    }
}
