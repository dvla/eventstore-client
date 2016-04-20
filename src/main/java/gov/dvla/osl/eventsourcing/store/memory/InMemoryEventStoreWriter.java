package gov.dvla.osl.eventsourcing.store.memory;

import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.api.EventStoreWriter;

import java.util.ConcurrentModificationException;
import java.util.List;

public class InMemoryEventStoreWriter extends InMemoryEventStoreReader implements EventStoreWriter {

    @Override
    public void store(String aggregateId, long expectedVersion, List<Event> events) {
        ListEventStream stream = loadEventStream(aggregateId);
        if (stream.version() != expectedVersion) {
            throw new ConcurrentModificationException("Stream has already been modified");
        }
        streams.put(aggregateId, stream.append(events));
        synchronized (transactions) {
            transactions.add(new Transaction(events));
        }
    }
}
