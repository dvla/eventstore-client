package gov.dvla.osl.eventsourcing.api;

import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.Entry;
import rx.Observable;
import rx.functions.Func0;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public interface EventStoreReader<V> {
	EventStream<Long> loadEventStream(String aggregateId);
	Observable<EventStoreEvent> all();
	Observable<EventStoreEvent> streamFrom(String streamName);
	Observable<Entry> readStreamEventsForward(Func0<Integer> getNextVersionNumber);
    void shutdown();
}
