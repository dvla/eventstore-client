package gov.dvla.osl.eventsourcing.store.memory;

import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public abstract class InMemoryEventStore {
    final Map<String, ListEventStream> streams = new ConcurrentHashMap<>();
    final TreeSet<Transaction> transactions = new TreeSet<Transaction>();
}
