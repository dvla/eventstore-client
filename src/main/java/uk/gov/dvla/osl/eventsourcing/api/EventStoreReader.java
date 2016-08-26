package uk.gov.dvla.osl.eventsourcing.api;

import uk.gov.dvla.osl.eventsourcing.store.http.entity.Entry;
import rx.Observable;

public interface EventStoreReader {

    EventStream loadEventStream(final String streamName);

    EventStream loadEventStream(final String streamName,
                                final int start);

    EventStream loadEventStreamWithLastEvent(final String streamName);

    Observable<Entry> readStreamEventsForward(final String streamName,
                                              final int start,
                                              final int count,
                                              final boolean keepAlive);

    Observable<Entry> readStreamEventsBackward(final String streamName,
                                               final int start,
                                               final int count,
                                               final boolean keepAlive);

    Observable<Entry> readLastEvent(final String streamName);

    void shutdown();
}
