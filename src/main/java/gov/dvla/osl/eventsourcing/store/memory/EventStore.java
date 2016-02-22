package gov.dvla.osl.eventsourcing.store.memory;

import gov.dvla.osl.eventsourcing.api.Event;
import rx.Observable;
import gov.dvla.osl.eventsourcing.api.EventStoreEvent;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface EventStore<V> {
	EventStream<Long> loadEventStream(UUID aggregateId);
	void store(UUID aggregateId, long version, List<Event> events);
	void storeBlocking(UUID aggregateId, long version, List<Event> events, long timeout, TimeUnit timeUnit);
	Observable<EventStoreEvent> all();
	Observable<EventStoreEvent> streamFrom(String streamName);
}
