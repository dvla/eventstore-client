package gov.dvla.osl.eventsourcing.store.http.writer;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.api.EventStoreWriter;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.exception.EventStoreClientTechnicalException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import gov.dvla.osl.eventsourcing.store.http.EventStoreService;
import gov.dvla.osl.eventsourcing.store.http.ServiceGenerator;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import retrofit2.Call;
import retrofit2.Response;

public class HttpEventStoreWriter implements EventStoreWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpEventStoreWriter.class);
    private static final String WRITING_EVENT_ERROR = "Error in Writing event to event store";

    private EventStoreConfiguration configuration;
    private final ObjectMapper mapper;

    public HttpEventStoreWriter(EventStoreConfiguration configuration, final ObjectMapper mapper) {
        this.configuration = configuration;
        this.mapper = mapper;
    }

    @Override
    public void store(String streamName, long expectedVersion, List<Event> events) {

        EventStoreService eventService = ServiceGenerator.createService(EventStoreService.class, this.configuration);

        List<AddEventRequest> addEventRequests = events.stream()
                .map((event) -> new AddEventRequest(UUID.randomUUID(),
                        event.getClass().getTypeName(),
                        event))
                .collect(Collectors.toList());

        try {
            String body = mapper.writeValueAsString(addEventRequests);

            Call<Void> call = eventService.postEvents(expectedVersion,
                    streamName,
                    RequestBody.create(MediaType.parse("text/plain"),
                            body));

            Response<Void> response = call.execute();

            if (!response.isSuccess()) {
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
    public void store(String streamName, long expectedVersion, Event event) {
        List<Event> events = new ArrayList<>();
        events.add(event);
        store(streamName, expectedVersion, events);
    }
}
