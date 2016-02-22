package gov.dvla.osl.eventsourcing.projection;

import gov.dvla.osl.eventsourcing.api.EventStoreEvent;

import java.io.IOException;

public interface EventProcessor {
    void processEvent(EventStoreEvent event) throws ClassNotFoundException, IOException;
    ProjectionVersionService projectionVersionService();
}
