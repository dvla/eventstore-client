package gov.dvla.osl.eventsourcing.api;

public interface EventDeserialiser {
    Event deserialise(String data, String eventType);
}
