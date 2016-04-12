package httpeventstore;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.store.httpeventstore.EventStoreService;
import gov.dvla.osl.eventsourcing.store.httpeventstore.EventStoreStream;
import gov.dvla.osl.eventsourcing.store.httpeventstore.EventStoreWriter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import uk.gov.dvla.osl.memory.SomeEvent;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static javafx.beans.binding.Bindings.when;
import static org.hamcrest.MatcherAssert.assertThat;


public class HttpEventStoreWriterTest {

    private static final Logger LOGGER =  LoggerFactory.getLogger(HttpEventStoreWriterTest.class);

    private final String STREAM_NAME = "testStream";
    private EventStoreWriter writer;
    private EventStoreConfiguration configuration;



    private static final String HOST = "localhost";
    private static final Integer PORT = 2113;
    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "pass";
    private static final String PATH = "/streams/.+";



    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);


    @Before
    public void setUp() throws IOException {

        configuration = new EventStoreConfiguration(HOST, PORT, USERNAME, PASSWORD);
        writer = new EventStoreWriter(configuration, new ObjectMapper());
    }

    @Test
    public void testWriteEventSuccessfully() throws IOException {
        if(!wireMockRule.isRunning()) {
            wireMockRule.start();
        }

        UUID id= UUID.randomUUID();
        LOGGER.info("ID" + id);
        String url = PATH + STREAM_NAME;

        stubFor(post(urlMatching("/streams/testStream"))
                .willReturn(aResponse().withStatus(201)));

        java.util.List<Event> events = new ArrayList<>();

        events.add(new SomeEvent(id, "forename1", "surname1", "email1"));

        writer.writeEvents(STREAM_NAME , 0, events);

        verify(postRequestedFor(urlEqualTo("/streams/testStream")));

    }







}
