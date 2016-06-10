package uk.gov.dvla.osl.eventsourcing.store.http.healthcheck;

import uk.gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import uk.gov.dvla.osl.eventsourcing.store.http.EventStoreService;
import uk.gov.dvla.osl.eventsourcing.store.http.entity.HealthCheck;
import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.io.IOException;

public class EventStoreHealthCheck {

    final Retrofit retrofit;

    public EventStoreHealthCheck(final EventStoreConfiguration configuration) {

        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(configuration.getScheme())
                .host(configuration.getHost())
                .port(configuration.getPort())
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(httpUrl)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
    }

    public boolean check() {

        final EventStoreService service = retrofit.create(EventStoreService.class);

        final Call<HealthCheck> ping = service.ping();

        try {
            final Response<HealthCheck> response = ping.execute();
             return response.isSuccessful();
        } catch (IOException e) {
            return false;
        }
    }
}
