package httpeventstore;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.store.httpeventstore.HttpEventStoreWriter;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dvla.osl.memory.SomeEvent;

import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class HttpHttpEventStoreWriterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpHttpEventStoreWriterTest.class);

    private final String STREAM_NAME = "testStream";
    private HttpEventStoreWriter writer;
    private EventStoreConfiguration configuration;

    private static final String SCHEME = "http";
    private static final String HOST = "localhost";
    private static final Integer PORT = 2113;
    private static final String USERNAME = "testUser";
    private static final String PASSWORD = "pass";
    private static final String PATH = "/streams/.+";

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(PORT);

    @Before
    public void setUp() throws IOException {
        configuration = new EventStoreConfiguration(SCHEME, HOST, PORT, USERNAME, PASSWORD);
        writer = new HttpEventStoreWriter(configuration, new ObjectMapper());
    }

    @Test
    public void testWriteEventSuccessfully() throws IOException {

        if (!wireMockRule.isRunning()) {
            wireMockRule.start();
        }

        UUID id = UUID.randomUUID();
        LOGGER.info("ID" + id);

        stubFor(post(urlMatching("/streams/testStream"))
                .willReturn(aResponse().withStatus(201)));

        java.util.List<Event> events = new ArrayList<>();

        events.add(new SomeEvent(id, "forename1", "surname1", "email1"));

        writer.writeEvents(STREAM_NAME, 0, events);

        verify(postRequestedFor(urlEqualTo("/streams/testStream")));
    }
}
