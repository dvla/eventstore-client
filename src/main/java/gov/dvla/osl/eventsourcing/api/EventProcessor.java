package gov.dvla.osl.eventsourcing.api;

import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.Entry;

import java.io.IOException;

public interface EventProcessor {
    void processEvent(Entry event) throws ClassNotFoundException, IOException;
}
