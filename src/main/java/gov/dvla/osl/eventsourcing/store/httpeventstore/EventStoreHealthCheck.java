package gov.dvla.osl.eventsourcing.store.httpeventstore;

import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.HealthCheck;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;

public class EventStoreHealthCheck {

    Retrofit retrofit;

    public EventStoreHealthCheck(String baseUrl) {
        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }

    public boolean check() {

        EventStoreService service = retrofit.create(EventStoreService.class);

        Call<HealthCheck> ping = service.ping();

        try {
            Response<HealthCheck> response = ping.execute();
             return response.isSuccess();
        } catch (IOException e) {
            return false;
        }
    }
}
