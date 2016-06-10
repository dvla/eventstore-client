package uk.gov.dvla.osl.eventsourcing.api;

import uk.gov.dvla.osl.eventsourcing.api.Event;

public interface EventDeserialiser {
    Event deserialise(final String data, final String eventType);
}