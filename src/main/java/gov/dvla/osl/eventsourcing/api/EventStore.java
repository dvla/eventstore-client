package gov.dvla.osl.eventsourcing.api;

public interface EventStore<V> extends EventStoreReader<V>, EventStoreWriter {
}
