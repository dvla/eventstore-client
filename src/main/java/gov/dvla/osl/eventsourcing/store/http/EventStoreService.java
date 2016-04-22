package gov.dvla.osl.eventsourcing.store.http;

import gov.dvla.osl.eventsourcing.store.http.entity.EventStreamData;
import gov.dvla.osl.eventsourcing.store.http.entity.HealthCheck;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.List;

public interface EventStoreService {

    @Headers("Accept:application/vnd.eventstore.events+json")
    @GET()
    Call<EventStreamData> getEventStreamData(@Header("ES-LongPoll") String longPoll,
                            @Url String url);

    @GET("ping")
    Call<HealthCheck> ping();

    @Headers("Content-Type:application/vnd.eventstore.events+json")
    @POST(value = "/streams/{streamName}")
    Call<Void> postEvents(@Header("ES-ExpectedVersion") long expectedVersion,
                          @Path("streamName") final String streamName,
                          @Body final RequestBody body);

    @Headers("Content-Type:application/json")
    @POST(value = "/streams/{streamName}")
    Call<Void> postEvent(@Header("ES-EventType") String eventType,
                         @Header("ES-EventId") String eventId,
                         @Header("ES-ExpectedVersion") long expectedVersion,
                          @Path("streamName") final String streamName,
                          @Body final RequestBody body);
}