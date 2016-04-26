package gov.dvla.osl.eventsourcing.api;

import gov.dvla.osl.eventsourcing.store.http.entity.Entry;
import rx.Observable;
import rx.functions.Func0;

public interface EventStoreReader<V> {
    EventStream loadEventStream(final String streamName);
	Observable<Entry> readStreamEventsForward(final Func0<Integer> getNextVersionNumber);
    void shutdown();
}
