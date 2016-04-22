package gov.dvla.osl.eventsourcing.store.http.healthcheck;

import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.store.http.EventStoreService;
import gov.dvla.osl.eventsourcing.store.http.entity.HealthCheck;
import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;

public class EventStoreHealthCheck {

    Retrofit retrofit;

    public EventStoreHealthCheck(EventStoreConfiguration configuration) {

        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(configuration.getScheme())
                .host(configuration.getHost())
                .port(configuration.getHttpPort())
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(httpUrl)
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
