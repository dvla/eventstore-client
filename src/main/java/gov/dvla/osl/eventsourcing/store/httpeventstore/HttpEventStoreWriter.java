package gov.dvla.osl.eventsourcing.store.httpeventstore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.exception.EventStoreClientTechnicalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

//TODO: Pass timeout through using config for the tcp version of this
//
//private static final long DEFAULT_TIMEOUT_SECONDS = 1;
//private static final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.SECONDS;

public class HttpEventStoreWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpEventStoreWriter.class);
    private static final String WRITING_EVENT_ERROR = "Error in Writing event to event store";

    private EventStoreConfiguration configuration;
    private final ObjectMapper mapper;

    public HttpEventStoreWriter(EventStoreConfiguration configuration, final ObjectMapper mapper) {
        this.configuration = configuration;
        this.mapper = mapper;
    }

    public void writeEvents(String streamName, long expectedVersion, List<Event> events) throws IOException {

        EventStoreService eventService = ServiceGenerator.createService(EventStoreService.class, this.configuration);

        List<AddEventRequest> addEventRequests = events.stream()
                .map(this::constructEventRequest)
                .collect(Collectors.toList());

        try {
            final Response<Void> writhingEventsResponse = eventService.postEvents(expectedVersion, streamName, addEventRequests).execute();

            if (!writhingEventsResponse.isSuccess()) {
                LOGGER.error(WRITING_EVENT_ERROR);
                throw new EventStoreClientTechnicalException(WRITING_EVENT_ERROR);
            }
        } catch (IOException e) {
            LOGGER.error(WRITING_EVENT_ERROR + ": "
                    + e.getMessage());
            throw new RuntimeException(WRITING_EVENT_ERROR);
        }
    }

    private AddEventRequest constructEventRequest(Event event) {

        AddEventRequest addEventRequest = null;
        try {
            addEventRequest = new AddEventRequest(UUID.randomUUID(),
                    event.getClass().getTypeName(),
                    mapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            LOGGER.error(e.toString(), e);
        }

        return addEventRequest;
    }
}
