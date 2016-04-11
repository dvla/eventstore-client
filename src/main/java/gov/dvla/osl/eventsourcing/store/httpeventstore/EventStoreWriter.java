package gov.dvla.osl.eventsourcing.store.httpeventstore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class EventStoreWriter {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventStoreWriter.class);

    private EventStoreConfiguration configuration;
    private final ObjectMapper mapper;

    public EventStoreWriter(EventStoreConfiguration configuration,
            final ObjectMapper mapper) {

        this.configuration = configuration;
        this.mapper = mapper;
    }

    public void writeEvents(String streamName, long expectedVersion, List<Event> events) throws IOException {

        EventStoreService eventService = ServiceGenerator.createService(EventStoreService.class, this.configuration);

        List<AddEventRequest> addEventRequests = events.stream()
                .map(this::constructEventRequest)
                .collect(Collectors.toList());

        eventService.postEvents(expectedVersion, streamName, addEventRequests).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
            }

            @Override
            public void onFailure(Call<Void> call, Throwable throwable) {
                LOGGER.error(throwable.getMessage(), throwable);
            }
        });
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
