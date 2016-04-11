package gov.dvla.osl.eventsourcing.store.httpeventstore;

import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.EventStreamData;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.HealthCheck;
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
    @POST(value = "/streams/{streamId}")
    Call<Void> postEvents(@Header("ES-ExpectedVersion") long expectedVersion,
                          @Path("streamId") final String streamId,
                          @Body final List<AddEventRequest> listOfEvents);
}