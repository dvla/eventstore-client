package gov.dvla.osl.eventsourcing.api;

import java.util.List;

public interface EventStoreWriter {
    void store(String aggregateId, long expectedVersion, List<Event> events);
}
