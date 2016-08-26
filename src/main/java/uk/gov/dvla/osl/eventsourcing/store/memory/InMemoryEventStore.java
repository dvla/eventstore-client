package uk.gov.dvla.osl.eventsourcing.store.memory;

import uk.gov.dvla.osl.eventsourcing.api.Event;
import uk.gov.dvla.osl.eventsourcing.api.EventDeserialiser;
import uk.gov.dvla.osl.eventsourcing.api.EventStore;
import uk.gov.dvla.osl.eventsourcing.api.EventStream;
import uk.gov.dvla.osl.eventsourcing.api.ListEventStream;
import uk.gov.dvla.osl.eventsourcing.impl.DefaultEventDeserialiser;
import uk.gov.dvla.osl.eventsourcing.store.http.entity.Entry;
import rx.Observable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Deprecated
public class InMemoryEventStore implements EventStore {
    final Map<String, ListEventStream> streams = new ConcurrentHashMap<>();
    final TreeSet<Transaction> transactions = new TreeSet<Transaction>();
    final EventDeserialiser eventDeserialiser = new DefaultEventDeserialiser();

    @Override
    public ListEventStream loadEventStream(final String streamName) {
        ListEventStream eventStream = streams.get(streamName);
        if (eventStream == null) {
            eventStream = new ListEventStream();
            streams.put(streamName, eventStream);
        }
        return eventStream;
    }

    @Override
    public EventStream loadEventStream(String streamName, int start) {
        return loadEventStream(streamName);
    }

    @Override
    public EventStream loadEventStreamWithLastEvent(String streamName) {
        return null;
    }

    public EventStream loadEventsAfter(final Long timestamp) {
        // include all events after this timestamp, except the events with the current timestamp
        // since new events might be added with the current timestamp
        final List<Event> events = new LinkedList<>();
        final long now;
        synchronized (transactions) {
            now = System.currentTimeMillis();
            for (Transaction t : transactions.tailSet(new Transaction(timestamp)).headSet(new Transaction(now))) {
                events.addAll(t.events);
            }
        }
        return new ListEventStream(now-1, events);
    }

    @Override
    public Observable<Entry> readStreamEventsForward(final String streamName,
                                                     final int start,
                                                     final int count,
                                                     final boolean keepAlive) {
       return null;
    }

    @Override
    public Observable<Entry> readStreamEventsBackward(final String streamName,
                                                      final int start,
                                                      final int count,
                                                      final boolean keepAlive) {
        return null;
    }

    @Override
    public Observable<Entry> readLastEvent(String streamName) {

        return null;
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void store(final String streamName, final long expectedVersion, final List<Event> events) {
        final ListEventStream stream = loadEventStream(streamName);
        if (stream.version() != expectedVersion) {
            throw new ConcurrentModificationException("Stream has already been modified.  Stream.version=" + stream.version() + ", expectedVersion=" + expectedVersion);
        }
        streams.put(streamName, stream.append(events));
        synchronized (transactions) {
            transactions.add(new Transaction(events));
        }
    }

    @Override
    public void store(final String streamName, final long expectedVersion, final Event event) {
        final List<Event> events = new ArrayList<>();
        events.add(event);
        store(streamName, expectedVersion, events);
    }
}
