package gov.dvla.osl.eventsourcing.exception;

public class EventHandlerException extends RuntimeException {
    public EventHandlerException(String eventType, Throwable exception) {
        super(String.format("Error handling event: %s ", eventType), exception);
    }
}
