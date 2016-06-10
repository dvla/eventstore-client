package uk.gov.dvla.osl.eventsourcing.store.http.reader;

import uk.gov.dvla.osl.eventsourcing.api.DataFetcher;
import uk.gov.dvla.osl.eventsourcing.exception.EventStoreClientTechnicalException;
import uk.gov.dvla.osl.eventsourcing.store.http.EventStoreService;
import uk.gov.dvla.osl.eventsourcing.store.http.entity.EventStreamData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

public class StreamDataFetcher implements DataFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpEventStoreReader.class);
    final private EventStoreService service;
    final private String longPollSeconds;

    public StreamDataFetcher(final EventStoreService service,
                             final String longPollSeconds) {
        this.service = service;
        this.longPollSeconds = longPollSeconds;
    }

    @Override
    public EventStreamData fetchStreamData(final String url, final boolean longPoll) throws IOException {

        if (longPoll)
            LOGGER.info("Starting long-poll with value of " + longPollSeconds);

        final Call<EventStreamData> eventStream = service.getEventStreamData(longPoll ? longPollSeconds : null, url + "?embed=body");

        final Response<EventStreamData> response = eventStream.execute();

        if (response.isSuccessful())
            return response.body();
        else
            throw new EventStoreClientTechnicalException(String.format("GET failed on %s with status %d", url, response.code()));
    }
}
