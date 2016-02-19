package uk.gov.dvla.osl.es.store.memory;

import rx.Observable;
import uk.gov.dvla.osl.es.api.Event;
import uk.gov.dvla.osl.es.api.EventStoreEvent;

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
