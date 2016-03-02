package gov.dvla.osl.eventsourcing.exception;

/**
 *
 * Indicates an unknown stream has been requested within EventStore.
 *
 * TODO: We need to refactor the client lib to handle EXCEPTIONS !!
 *       this is a major refactor job.
 *       for this sprint we are throwing a runtime exception  - MS
 *
 */
public class EventStoreClientUnknownStreamException extends RuntimeException {

    public EventStoreClientUnknownStreamException(String message) {
        super(message);
    }
}
