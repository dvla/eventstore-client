package gov.dvla.osl.eventsourcing.api;

import rx.Observable;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface EventStoreReader<V> {
	EventStream<Long> loadEventStream(UUID aggregateId);
	Observable<EventStoreEvent> all();
	Observable<EventStoreEvent> streamFrom(String streamName);
}
