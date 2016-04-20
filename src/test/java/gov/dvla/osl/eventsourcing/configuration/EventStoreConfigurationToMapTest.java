package gov.dvla.osl.eventsourcing.configuration;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import gov.dvla.osl.eventsourcing.store.tcpeventstore.TcpEventStoreReader;

import static gov.dvla.osl.eventsourcing.configuration.EventStoreConfigurationToMap.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Simple test for converting eventstore configuration to a map, to be passed into the eventstore.
 * client config
 * @author Jeremy Prime
 * @since 1.0.0
 */
public class EventStoreConfigurationToMapTest {

    /**
     * Test scheme.
     */
    private static final String TEST_SCHEME = "http";

    /**
     * Test host name.
     */
    private static final String TEST_HOST_NAME = "testHost";

    /**
     * Test port.
     */
    private static final int TEST_PORT = 1111;

    /**
     * Test user.
     */
    private static final String TEST_USER = "testUser";

    /**
     * Test password.
     */
    private static final String TEST_PASSWORD = "testPassword";

    /**
     * Test list of gossip seeds.
     */
    private static final List<String> TEST_GOSSIP_SEEDS =
            Arrays.asList("gossip1", "gossip2", "gossip3");

    /**
     * Test dns timeout value.
     */
    private static final String TEST_DNS_TIMEOUT = "3s";

    /**
     * Test external gossip port.
     */
    private static final int TEST_EXTERNAL_GOSSIP_PORT = 30454;

    /**
     * Test discover attempts value.
     */
    private static final int TEST_MAX_DISCOVER_ATTEMPTS = 50;

    /**
     * Test discover attempt interval.
     */
    private static final String TEST_DISCOVER_ATTEMPT_INTERVAL = "750ms";

    /**
     * Test discovery interval.
     */
    private static final String TEST_DISCOVERY_INTERVAL = "2s";

    /**
     * Test gossip timeout.
     */
    private static final String TEST_GOSSIP_TIMEOUT = "1s";

    /**
     * Dev cluster host, used for testing the connection to a real cluster.
     */
    private static final String DEV_CLUSTER_HOST = "172.16.4.51";

    /**
     * Sleep time in milliseconds for the connectivity test, to validate that logs show
     * successful connection.
     */
    private static final int CONNECTIVITY_TEST_SLEEP_TIME_MS = 10000;

    /**
     * Test conversion to a map of an eventstore configuration containing cluster configuration.
     * @throws Exception - tests can throw exceptions
     */
    @Test
    public void testAsMapWIthClusterInfo() throws Exception {

        final EventStoreConfiguration config = simpleClusteredConfiguration();

        final EventStoreConfigurationToMap configWrapper =
                new EventStoreConfigurationToMap(config);

        final Map<String, Object> injectedConfig = configWrapper.asMap();
        assertThat(injectedConfig.get(EVENTSTORE_CONFIG_HOST), is(TEST_HOST_NAME));
        assertThat(injectedConfig.get(EVENTSTORE_CONFIG_PORT), is(TEST_PORT));
        assertThat(injectedConfig.get(EVENTSTORE_CONFIG_USERID), is(TEST_USER));
        assertThat(injectedConfig.get(EVENTSTORE_CONFIG_PASSWORD), is(TEST_PASSWORD));
        assertThat(injectedConfig.get(EVENTSTORE_CONFIG_GOSSIP_SEEDS), is(TEST_GOSSIP_SEEDS));
        assertThat(injectedConfig.get(EVENTSTORE_CONFIG_DNS), is(nullValue()));
        assertThat(injectedConfig.get(EVENTSTORE_CONFIG_DNS_TIMEOUT), is(TEST_DNS_TIMEOUT));
        assertThat(injectedConfig.get(EVENTSTORE_CONFIG_GOSSIP_PORT),
                is(TEST_EXTERNAL_GOSSIP_PORT));
        assertThat(injectedConfig.get(EVENTSTORE_CONFIG_MAX_DISCOVER_ATTEMPTS),
                is(TEST_MAX_DISCOVER_ATTEMPTS));
        assertThat(injectedConfig.get(EVENTSTORE_CONFIG_DISCOVER_ATTEMPT_INTERVAL),
                is(TEST_DISCOVER_ATTEMPT_INTERVAL));
        assertThat(injectedConfig.get(EVENTSTORE_CONFIG_DISCOVERY_INTERVAL),
                is(TEST_DISCOVERY_INTERVAL));
        assertThat(injectedConfig.get(EVENTSTORE_CONFIG_GOSSIP_TIMEOUT), is(TEST_GOSSIP_TIMEOUT));
    }

    /**
     * Test connectivity to a real eventstore cluster.
     *
     * This test intentionally ignored as it's fully dependent on infrastructure which may not
     * always be present, but it is useful for testing that the cluster configuration can be
     * used to connect to a known cluster. Simply tweak the configuration in the test to point
     * to the cluster you want to test and manually run the test.
     * @throws Exception - tests can throw exceptions
     */
    @Ignore
    @Test
    public void testConnectToKnownCluster() throws Exception {
        final EventStoreConfiguration eventStoreConfig = new EventStoreConfiguration(
                TEST_SCHEME,
                DEV_CLUSTER_HOST,
                1111,
                TEST_USER,
                TEST_PASSWORD);
        final EventStoreClusterConfiguration clusterConfig = new EventStoreClusterConfiguration();

        final List<String> gossipSeeds = Arrays.asList(DEV_CLUSTER_HOST + ":2114",
                DEV_CLUSTER_HOST + ":3114",
                DEV_CLUSTER_HOST + ":4114");
        clusterConfig.setGossipSeeds(gossipSeeds);
        clusterConfig.setDnsLookupTimeout(TEST_DNS_TIMEOUT);
        clusterConfig.setExternalGossipPort(TEST_EXTERNAL_GOSSIP_PORT);
        clusterConfig.setMaxDiscoverAttempts(TEST_MAX_DISCOVER_ATTEMPTS);
        clusterConfig.setDiscoverAttemptInterval(TEST_DISCOVER_ATTEMPT_INTERVAL);
        clusterConfig.setDiscoveryInterval(TEST_DISCOVERY_INTERVAL);
        clusterConfig.setGossipTimeout(TEST_GOSSIP_TIMEOUT);

        eventStoreConfig.setCluster(clusterConfig);
        final TcpEventStoreReader store = new TcpEventStoreReader("test-stream",
                new ObjectMapper(),
                eventStoreConfig
                );

        // Ugly cheat to let us read the log and see we've connected
        Thread.sleep(CONNECTIVITY_TEST_SLEEP_TIME_MS);

    }


    /**
     * Generate a simple eventstore configuration containing cluster information
     * @return EventsStoreConfiguration - the configuration
     */
    private EventStoreConfiguration simpleClusteredConfiguration() {
        final EventStoreConfiguration eventStoreConfig = new EventStoreConfiguration(
                TEST_SCHEME,
                TEST_HOST_NAME,
                TEST_PORT,
                TEST_USER,
                TEST_PASSWORD);
        final EventStoreClusterConfiguration clusterConfig = new EventStoreClusterConfiguration();

        clusterConfig.setGossipSeeds(TEST_GOSSIP_SEEDS);
        clusterConfig.setDnsLookupTimeout(TEST_DNS_TIMEOUT);
        clusterConfig.setExternalGossipPort(TEST_EXTERNAL_GOSSIP_PORT);
        clusterConfig.setMaxDiscoverAttempts(TEST_MAX_DISCOVER_ATTEMPTS);
        clusterConfig.setDiscoverAttemptInterval(TEST_DISCOVER_ATTEMPT_INTERVAL);
        clusterConfig.setDiscoveryInterval(TEST_DISCOVERY_INTERVAL);
        clusterConfig.setGossipTimeout(TEST_GOSSIP_TIMEOUT);
        eventStoreConfig.setCluster(clusterConfig);

        return eventStoreConfig;

    }
}