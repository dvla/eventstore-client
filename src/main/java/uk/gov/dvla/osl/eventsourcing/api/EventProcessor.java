package uk.gov.dvla.osl.eventsourcing.api;

import uk.gov.dvla.osl.eventsourcing.store.http.entity.Entry;

public interface EventProcessor {
    void processEvent(final Entry event);
}
