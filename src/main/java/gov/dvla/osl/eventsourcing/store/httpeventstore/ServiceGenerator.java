package gov.dvla.osl.eventsourcing.store.httpeventstore;

import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import okhttp3.*;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.Base64;
import java.util.concurrent.TimeUnit;

public class ServiceGenerator {

    private static OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

    private static Retrofit.Builder builder =
            new Retrofit.Builder()
                    .addConverterFactory(JacksonConverterFactory.create());

    public static <S> S createService(Class<S> serviceClass, EventStoreConfiguration configuration) {
        if (configuration.getUserId() != null && configuration.getPassword() != null) {
            String credentials = configuration.getUserId() + ":" + configuration.getPassword();
            final String basic = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

            httpClient.addInterceptor(chain -> {
                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", basic)
                        .header("Accept", "application/vnd.eventstore.atom+json")
                        .method(original.method(), original.body());

                Request request = requestBuilder.build();
                return chain.proceed(request);
            });
        }

        OkHttpClient client = httpClient.readTimeout(31, TimeUnit.SECONDS).build();
        Retrofit retrofit = builder.baseUrl(configuration.getHost() + ":" + configuration.getPort()).client(client).build();
        return retrofit.create(serviceClass);
    }
}
