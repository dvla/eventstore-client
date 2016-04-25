package gov.dvla.osl.eventsourcing.api;

import gov.dvla.osl.eventsourcing.store.http.entity.Entry;
import rx.Observable;
import rx.functions.Func0;

public interface EventStoreReader<V> {
    EventStream loadEventStream(String streamName);
	Observable<Entry> readStreamEventsForward(Func0<Integer> getNextVersionNumber);
    void shutdown();
}
