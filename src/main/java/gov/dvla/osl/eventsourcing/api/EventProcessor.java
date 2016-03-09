package gov.dvla.osl.eventsourcing.api;

import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.Entry;

public interface EventProcessor {
    void processEvent(Entry event);
}
