package gov.dvla.osl.eventsourcing.api;

import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.EventStreamData;

import java.io.IOException;

public interface DataFetcher {
    EventStreamData getStreamData(String url, boolean longPoll) throws IOException;
}
