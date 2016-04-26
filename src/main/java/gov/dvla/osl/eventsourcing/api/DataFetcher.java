package gov.dvla.osl.eventsourcing.api;

import gov.dvla.osl.eventsourcing.store.http.entity.EventStreamData;

import java.io.IOException;

public interface DataFetcher {
    EventStreamData getStreamData(final String url, final boolean longPoll) throws IOException;
}
