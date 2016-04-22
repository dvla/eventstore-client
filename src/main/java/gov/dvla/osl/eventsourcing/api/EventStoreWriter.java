package gov.dvla.osl.eventsourcing.api;

import java.util.List;

public interface EventStoreWriter {
    void store(String streamName, long expectedVersion, List<Event> events);
    void store(String streamName, long expectedVersion, Event event);
}
