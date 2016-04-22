package gov.dvla.osl.eventsourcing.store.memory;

import gov.dvla.osl.eventsourcing.api.*;
import gov.dvla.osl.eventsourcing.store.http.entity.Entry;
import rx.Observable;
import rx.functions.Func0;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryEventStore implements EventStore<Long> {
    final Map<String, ListEventStream> streams = new ConcurrentHashMap<>();
    final TreeSet<Transaction> transactions = new TreeSet<Transaction>();

    @Override
    public ListEventStream loadEventStream(String aggregateId) {
        ListEventStream eventStream = streams.get(aggregateId);
        if (eventStream == null) {
            eventStream = new ListEventStream();
            streams.put(aggregateId, eventStream);
        }
        return eventStream;
    }

    public EventStream loadEventsAfter(Long timestamp) {
        // include all events after this timestamp, except the events with the current timestamp
        // since new events might be added with the current timestamp
        List<Event> events = new LinkedList<>();
        long now;
        synchronized (transactions) {
            now = System.currentTimeMillis();
            for (Transaction t : transactions.tailSet(new Transaction(timestamp)).headSet(new Transaction(now))) {
                events.addAll(t.events);
            }
        }
        return new ListEventStream(now-1, events);
    }

    @Override
    public Observable<EventStoreEvent> all() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Observable<EventStoreEvent> streamFrom(String streamName) {
        return null;
    }

    @Override
    public Observable<Entry> readStreamEventsForward(Func0<Integer> getNextVersionNumber) {
        return null;
    }

    @Override
    public void shutdown() {

    }

    @Override
    public void store(String streamName, long expectedVersion, List<Event> events) {
        ListEventStream stream = loadEventStream(streamName);
        if (stream.version() != expectedVersion) {
            throw new ConcurrentModificationException("Stream has already been modified.  Stream.version=" + stream.version() + ", expectedVersion=" + expectedVersion);
        }
        streams.put(streamName, stream.append(events));
        synchronized (transactions) {
            transactions.add(new Transaction(events));
        }
    }

    @Override
    public void store(String streamName, long expectedVersion, Event event) {

    }
}
