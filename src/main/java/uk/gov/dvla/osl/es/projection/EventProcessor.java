package uk.gov.dvla.osl.es.projection;

import uk.gov.dvla.osl.es.api.EventStoreEvent;

import java.io.IOException;

public interface EventProcessor {
    void processEvent(EventStoreEvent event) throws ClassNotFoundException, IOException;
    ProjectionVersionService projectionVersionService();
}
