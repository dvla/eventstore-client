package gov.dvla.osl.eventsourcing.exception;

public class EventDeserialisationException extends RuntimeException {
    public EventDeserialisationException(String eventData, String eventType, Throwable exception) {
        super(String.format("Could not deserialise eventData: %s into eventType: %s", eventData, eventType), exception);
    }
}
