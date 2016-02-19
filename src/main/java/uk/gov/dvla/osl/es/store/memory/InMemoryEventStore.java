package uk.gov.dvla.osl.es.store.memory;

import rx.Observable;
import uk.gov.dvla.osl.es.api.Event;
import uk.gov.dvla.osl.es.api.EventStoreEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class InMemoryEventStore implements EventStore<Long> {
	private final Map<UUID, ListEventStream> streams = new ConcurrentHashMap<UUID, ListEventStream>();
	private final TreeSet<Transaction> transactions = new TreeSet<Transaction>();

	@Override
	public ListEventStream loadEventStream(UUID aggregateId) {
		ListEventStream eventStream = streams.get(aggregateId);
		if (eventStream == null) {
			eventStream = new ListEventStream();
			streams.put(aggregateId, eventStream);
		}
		return eventStream;
	}

	@Override
	public void store(UUID aggregateId, long version, List<Event> events) {
		ListEventStream stream = loadEventStream(aggregateId);
		if (stream.version() != version) {
			throw new ConcurrentModificationException("Stream has already been modified");
		}
		streams.put(aggregateId, stream.append(events));
		synchronized (transactions) {
			transactions.add(new Transaction(events));
		}
	}

	@Override
	public void storeBlocking(UUID aggregateId, long version, List<Event> events, long timeout, TimeUnit timeUnit) {
		store(aggregateId, version, events);
	}

	public EventStream<Long> loadEventsAfter(Long timestamp) {
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

}

class Transaction implements Comparable<Transaction> {
	public final List<? extends Event> events;
	private final long timestamp;
	
	public Transaction(long timestamp) {
		events = Collections.emptyList();
		this.timestamp = timestamp;
		
	}
	public Transaction(List<? extends Event> events) {
		this.events = events;
		this.timestamp = System.currentTimeMillis();
	}
	
	@Override
	public int compareTo(Transaction other) {
		if (timestamp < other.timestamp) {
			return -1;
		} else if (timestamp > other.timestamp) {
			return 1;
		}
		return 0;
	}
}
