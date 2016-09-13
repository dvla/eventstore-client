package uk.gov.dvla.osl.eventsourcing.api;

public interface EventProcessor<T> {
    void processEvent(final T event);
}
