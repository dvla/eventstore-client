package gov.dvla.osl.eventsourcing.configuration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;


import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test for the reading of eventstore configuration from yml files.
 * @since 1.0.0
 */
public class EventStoreConfigurationReadingTest {

    /**
     * Gossip port which appears in the test yml file.
     */
    private static final int TEST_GOSSIP_PORT = 30778;

    /**
     * Max discover attempts from the test yml file.
     */
    private static final int TEST_MAX_DISCOVER_ATTEMPTS = 10;

    /**
     * One second.
     */
    private static final String ONE_SECOND = "1s";

    /**
     * Check parsing of fragment with a correctly structured cluster section.
     * @throws Exception - it's legitimate for tests to throw exception
     */
    @Test
    public void withValidClusterInfo() throws Exception {
        final File inputYml =
                new File("src/test/resources/test-config-files/test-cluster-config.yml");
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        final EventStoreConfiguration configuration = mapper.readValue(inputYml,
                EventStoreConfiguration.class);
        final EventStoreClusterConfiguration clusterConfig = configuration.getCluster();
        final List<String> gossipSeeds = clusterConfig.getGossipSeeds();
        assertThat(gossipSeeds, is(Arrays.asList("seed1", "seed2")));
        assertThat(clusterConfig.getDns(), is(nullValue()));
        assertThat(clusterConfig.getDnsLookupTimeout(), is("2s"));
        assertThat(clusterConfig.getExternalGossipPort(), is(TEST_GOSSIP_PORT));
        assertThat(clusterConfig.getMaxDiscoverAttempts(), is(TEST_MAX_DISCOVER_ATTEMPTS));
        assertThat(clusterConfig.getDiscoverAttemptInterval(), is("500ms"));
        assertThat(clusterConfig.getDiscoveryInterval(), is(ONE_SECOND));
        assertThat(clusterConfig.getGossipTimeout(), is(ONE_SECOND));

    }

}