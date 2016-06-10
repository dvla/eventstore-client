package uk.gov.dvla.osl.eventsourcing.api;

import java.util.List;

import uk.gov.dvla.osl.eventsourcing.api.Event;

public interface EventStoreWriter {
    void store(final String streamName, final long expectedVersion, final List<Event> events);
    void store(final String streamName, final long expectedVersion, final Event event);
}
