package uk.gov.dvla.osl.es.exception;

/**
 *
 * Indicates an unknown stream has been requested within EventStore.
 *
 * TODO: We need to refactor the client lib to handle EXCEPTIONS !!
 *       this is a major refactor job.
 *       for this sprint we are throwing a runtime exception  - MS
 *
 * @author Martin Shadbolt
 * @since 1.0.0
 */
public class EventStoreClientUnknownStreamException extends RuntimeException {

    public EventStoreClientUnknownStreamException(String message) {
        super(message);
    }
}
