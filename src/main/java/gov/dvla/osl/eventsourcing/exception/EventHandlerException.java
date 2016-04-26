package gov.dvla.osl.eventsourcing.exception;

public class EventHandlerException extends RuntimeException {
    public EventHandlerException(final String eventType, final Throwable exception) {
        super(String.format("Error handling event: %s ", eventType), exception);
    }
}
