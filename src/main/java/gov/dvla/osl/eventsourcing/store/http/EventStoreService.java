package gov.dvla.osl.eventsourcing.store.http;

import gov.dvla.osl.eventsourcing.store.http.entity.EventStreamData;
import gov.dvla.osl.eventsourcing.store.http.entity.HealthCheck;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface EventStoreService {

    @GET()
    Call<EventStreamData> getEventStreamData(@Header("ES-LongPoll") String longPoll,
                            @Url String url);

    @GET("ping")
    Call<HealthCheck> ping();
    @Headers({"Content-Type:application/vnd.eventstore.events+json"})
    @POST(value = "/streams/{streamName}")
    Call<Void> postEvents(@Header("ES-ExpectedVersion") long expectedVersion,
                          @Path("streamName") final String streamName,
                          @Body final List<AddEventRequest> listOfEvents);
}