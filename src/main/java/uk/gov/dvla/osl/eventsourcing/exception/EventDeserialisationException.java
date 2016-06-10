package uk.gov.dvla.osl.eventsourcing.exception;

public class EventDeserialisationException extends RuntimeException {
    public EventDeserialisationException(final String eventData, final String eventType, final Throwable exception) {
        super(String.format("Could not deserialise eventData: %s into eventType: %s", eventData, eventType), exception);
    }
}
