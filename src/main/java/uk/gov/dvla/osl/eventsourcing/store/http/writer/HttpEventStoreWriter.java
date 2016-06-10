package uk.gov.dvla.osl.eventsourcing.store.http.writer;

import com.fasterxml.jackson.databind.ObjectMapper;
import uk.gov.dvla.osl.eventsourcing.api.Event;
import uk.gov.dvla.osl.eventsourcing.api.EventStoreWriter;
import uk.gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import uk.gov.dvla.osl.eventsourcing.exception.EventStoreClientTechnicalException;
import uk.gov.dvla.osl.eventsourcing.store.http.EventStoreService;
import uk.gov.dvla.osl.eventsourcing.store.http.ServiceGenerator;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class HttpEventStoreWriter implements EventStoreWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpEventStoreWriter.class);
    private static final String WRITING_EVENT_ERROR = "Error in Writing event to event store";

    private final EventStoreConfiguration configuration;
    private final ObjectMapper mapper;

    public HttpEventStoreWriter(final EventStoreConfiguration configuration, final ObjectMapper mapper) {
        this.configuration = configuration;
        this.mapper = mapper;
    }

    @Override
    public void store(final String streamName, final long expectedVersion, final List<Event> events) {

        final EventStoreService eventService = ServiceGenerator.createService(EventStoreService.class, this.configuration);

        final List<AddEventRequest> addEventRequests = events.stream()
                .map((event) -> new AddEventRequest(UUID.randomUUID(),
                        event.getClass().getTypeName(),
                        event))
                .collect(Collectors.toList());

        try {
            final String body = mapper.writeValueAsString(addEventRequests);

            LOGGER.debug("Storing events: " + body);

            final Call<Void> call = eventService.postEvents(expectedVersion,
                    streamName,
                    RequestBody.create(MediaType.parse("text/plain"),
                            body));

            final Response<Void> response = call.execute();

            if (!response.isSuccessful()) {
                LOGGER.error(WRITING_EVENT_ERROR);
                throw new EventStoreClientTechnicalException(WRITING_EVENT_ERROR);
            }
        } catch (IOException e) {
            LOGGER.error(WRITING_EVENT_ERROR + ": "
                    + e.getMessage());
            throw new RuntimeException(WRITING_EVENT_ERROR);
        }
    }

    @Override
    public void store(final String streamName, final long expectedVersion, final Event event) {
        final List<Event> events = new ArrayList<>();
        events.add(event);
        store(streamName, expectedVersion, events);
    }
}
