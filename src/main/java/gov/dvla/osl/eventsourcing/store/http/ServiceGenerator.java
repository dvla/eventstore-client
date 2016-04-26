package gov.dvla.osl.eventsourcing.store.http;

import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class ServiceGenerator {

    private static final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static final Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addConverterFactory(JacksonConverterFactory.create());

    public static <S> S createService(final Class<S> serviceClass, final EventStoreConfiguration configuration) {
        if (configuration.getUserId() != null && configuration.getPassword() != null) {
            final String credentials = configuration.getUserId() + ":" + configuration.getPassword();
            final String basic = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

            httpClient.addInterceptor(chain -> {
                final Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", basic)
                        .header("Accept", "application/vnd.eventstore.atom+json")
                        .method(original.method(), original.body());

                final Request request = requestBuilder.build();
                return chain.proceed(request);
            });
        }

        final OkHttpClient client = httpClient.readTimeout(31, TimeUnit.SECONDS).build();

        final HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme(configuration.getScheme())
                .host(configuration.getHost())
                .port(configuration.getPort())
                .build();

        final Retrofit retrofit = builder.baseUrl(httpUrl).client(client).build();
        return retrofit.create(serviceClass);
    }
}
