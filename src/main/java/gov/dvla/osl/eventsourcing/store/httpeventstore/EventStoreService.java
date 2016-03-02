package gov.dvla.osl.eventsourcing.store.httpeventstore;

import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.EventStreamData;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface EventStoreService {
    @GET("streams/{stream}/{version}/forward/{pageSize}?embed=body")
    Call<EventStreamData> getData(@Path("stream") String stream, @Path("version") int version, @Path("pageSize") int pageSize);

    @GET()
    Call<EventStreamData> getEventStreamData(@Header("ES-LongPoll") String longPoll, @Url String url);
}