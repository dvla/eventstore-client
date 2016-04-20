package gov.dvla.osl.eventsourcing.store.memory;

import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.api.EventStoreReader;
import gov.dvla.osl.eventsourcing.api.EventStream;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.Entry;
import rx.Observable;
import gov.dvla.osl.eventsourcing.api.EventStoreEvent;
import rx.functions.Func0;

import java.util.*;

public class InMemoryEventStoreReader extends InMemoryEventStore implements EventStoreReader<Long>  {

	@Override
	public ListEventStream loadEventStream(String aggregateId) {
		ListEventStream eventStream = streams.get(aggregateId);
		if (eventStream == null) {
			eventStream = new ListEventStream();
			streams.put(aggregateId, eventStream);
		}
		return eventStream;
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

    @Override
    public Observable<Entry> readStreamEventsForward(Func0<Integer> getNextVersionNumber) {
        return null;
    }

    @Override
    public void shutdown() {

    }

}
