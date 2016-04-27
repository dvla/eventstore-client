package gov.dvla.osl.eventsourcing.store.http.reader;

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

    public void provideEntriesToSubscriber(final List<Entry> entries, final Subscriber subscriber) {
        entries.stream()
                .filter(this::validEvent)
                .sorted((o1, o2) -> o1.getEventNumber() - o2.getEventNumber())
                .forEach(event -> {
                    LOGGER.debug("Calling subscriber.onNext with event number " + event.getEventNumber());
                    subscriber.onNext(event);
                });
    }

    private boolean validEvent(final Entry entry) {
        return entry != null &&
                    entry.getEventNumber() != null &&
                    entry.getEventType() != null &&
                    !entry.getEventType().equals(HARD_DELETED_EVENT_TYPE);
    }
}
