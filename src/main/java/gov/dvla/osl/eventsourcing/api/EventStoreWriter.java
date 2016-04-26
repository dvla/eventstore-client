package gov.dvla.osl.eventsourcing.api;

import java.util.List;

public interface EventStoreWriter {
    void store(final String streamName, final long expectedVersion, final List<Event> events);
    void store(final String streamName, final long expectedVersion, final Event event);
}
