package gov.dvla.osl.eventsourcing.store.http.reader;

import gov.dvla.osl.eventsourcing.api.DataFetcher;
import gov.dvla.osl.eventsourcing.exception.EventStoreClientTechnicalException;
import gov.dvla.osl.eventsourcing.store.http.EventStoreService;
import gov.dvla.osl.eventsourcing.store.http.entity.EventStreamData;
import gov.dvla.osl.eventsourcing.store.http.reader.HttpEventStoreReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;

public class StreamDataFetcher implements DataFetcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpEventStoreReader.class);
    private EventStoreService service;
    private String longPollSeconds;

    public StreamDataFetcher(EventStoreService service,
                             String longPollSeconds) {
        this.service = service;
        this.longPollSeconds = longPollSeconds;
    }

    @Override
    public EventStreamData getStreamData(String url, boolean longPoll) throws IOException {

        if (longPoll)
            LOGGER.info("Starting long-poll with value of " + longPollSeconds);

        Call<EventStreamData> eventStream = service.getEventStreamData(longPoll ? longPollSeconds : null, url + "?embed=body");

        Response<EventStreamData> response = eventStream.execute();

        if (response.isSuccess())
            return response.body();
        else
            throw new EventStoreClientTechnicalException(String.format("GET failed on %s with status %d", url, response.code()));
    }
}
