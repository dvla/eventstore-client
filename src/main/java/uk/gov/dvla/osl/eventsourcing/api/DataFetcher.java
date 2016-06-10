package uk.gov.dvla.osl.eventsourcing.api;

import uk.gov.dvla.osl.eventsourcing.store.http.entity.EventStreamData;

import java.io.IOException;

public interface DataFetcher {
    EventStreamData fetchStreamData(final String url, final boolean longPoll) throws IOException;
}
