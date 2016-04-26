package gov.dvla.osl.eventsourcing.api;

public interface EventDeserialiser {
    Event deserialise(final String data, final String eventType);
}
