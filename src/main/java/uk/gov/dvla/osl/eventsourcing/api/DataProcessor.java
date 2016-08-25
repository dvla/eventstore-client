package uk.gov.dvla.osl.eventsourcing.api;

import rx.Subscriber;

public interface DataProcessor {
    void processData(final Subscriber subscriber,
                     final String streamName,
                     final boolean keepAlive,
                     final int start,
                     final int pageSize,
                     final ReadDirection readDirection) throws Exception;
    void shutDown();
}
