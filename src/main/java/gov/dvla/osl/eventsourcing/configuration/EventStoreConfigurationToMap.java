package gov.dvla.osl.eventsourcing.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration utility class to map Eventstore configuration into a format understood by
 * eventstore.JVM.
 *
 * Note that a sample config file into whose structure we are trying to map can be seen at
 * https://github.com/EventStore/EventStore.JVM/blob/master/src/main/resources/reference.conf
 *
 * The keys within the map correspond to the hierarchy within this file (an akka config file
 * for the akka subsystem which underlies an eventstore.JVM instance)
 *
 */
public class EventStoreConfigurationToMap {

    /**
     * Host.
     */
    public static final String EVENTSTORE_CONFIG_HOST = "eventstore.address.host";

    /**
     * Port.
     */
    public static final String EVENTSTORE_CONFIG_PORT = "eventstore.address.port";

    /**
     * User id.
     */
    public static final String EVENTSTORE_CONFIG_USERID = "eventstore.credentials.login";

    /**
     * Password.
     */
    public static final String EVENTSTORE_CONFIG_PASSWORD = "eventstore.credentials.password";

    /**
     * Reconnection attempts.
     */
    public static final String EVENTSTORE_CONFIG_RECONNECTION_ATTEMPTS =
            "eventstore.max-reconnections";

    /**
     * Health check URL.
     */
    public static final String EVENTSTORE_CONFIG_HEALTHCHECK_URL = "eventstore.healthcheck-url";

    /**
     * Prefix for eventstore cluster parameters in the map, this is how the eventstore.JVM
     * subsystem will be able to recognise those parameters as cluster parameters.
     */
    public static final String EVENTSTORE_CLUSTER_PREFIX = "eventstore.cluster.";

    /**
     * Gossip seeds configuration key in the mapped result.
     */
    public static final String EVENTSTORE_CONFIG_GOSSIP_SEEDS = EVENTSTORE_CLUSTER_PREFIX
            + "gossip-seeds";

    /**
     * Key for the dns parameter in the mapped result.
     */
    public static final String EVENTSTORE_CONFIG_DNS = EVENTSTORE_CLUSTER_PREFIX + "dns";

    /**
     * Key for the dns lookup timeout in the mapped result.
     */
    public static final String EVENTSTORE_CONFIG_DNS_TIMEOUT =
            EVENTSTORE_CLUSTER_PREFIX + "dns-lookup-timeout";

    /**
     * Key for the gossip port in the mapped result.
     */
    public static final String EVENTSTORE_CONFIG_GOSSIP_PORT =
            EVENTSTORE_CLUSTER_PREFIX + "external-gossip-port";

    /**
     * Key for the max discover attempts in the mapped result.
     */
    public static final String EVENTSTORE_CONFIG_MAX_DISCOVER_ATTEMPTS =
            EVENTSTORE_CLUSTER_PREFIX + "max-discover-attempts";

    /**
     * Key for the discover attempt interval in the mapped result.
     */
    public static final String EVENTSTORE_CONFIG_DISCOVER_ATTEMPT_INTERVAL =
            EVENTSTORE_CLUSTER_PREFIX + "discover-attempt-interval";

    /**
     * Key for the discovery interval in the mapped result.
     */
    public static final String EVENTSTORE_CONFIG_DISCOVERY_INTERVAL =
            EVENTSTORE_CLUSTER_PREFIX + "discovery-interval";

    /**
     * Key for the gossip timeout in the mapped result.
     */
    public static final String EVENTSTORE_CONFIG_GOSSIP_TIMEOUT =
            EVENTSTORE_CLUSTER_PREFIX + "gossip-timeout";

    /**
     * Config.
     */
    private final EventStoreConfiguration config;

    /**
     * Constructor.
     * @param config the config
     */
    public EventStoreConfigurationToMap(final EventStoreConfiguration config) {
        this.config = config;
    }

    /**
     * Convert config to Map.
     * @return the map
     */
    public Map<String, Object> asMap() {
        final Map<String, Object> mapped = new HashMap<>();
        mapped.put(EVENTSTORE_CONFIG_HOST, config.getHost());
        mapped.put(EVENTSTORE_CONFIG_PORT, config.getPort());
        mapped.put(EVENTSTORE_CONFIG_USERID, config.getUserId());
        mapped.put(EVENTSTORE_CONFIG_PASSWORD, config.getPassword());
        mapped.put(EVENTSTORE_CONFIG_RECONNECTION_ATTEMPTS, config.getReconnectionAttempts());
        mapped.put(EVENTSTORE_CONFIG_HEALTHCHECK_URL, config.getHealthCheckUrl());
        final EventStoreClusterConfiguration clusterConfig = config.getCluster();
        if (clusterConfig != null) {
            mapped.put(EVENTSTORE_CONFIG_GOSSIP_SEEDS, clusterConfig.getGossipSeeds());
            mapped.put(EVENTSTORE_CONFIG_DNS, clusterConfig.getDns());
            mapped.put(EVENTSTORE_CONFIG_DNS_TIMEOUT, clusterConfig.getDnsLookupTimeout());
            mapped.put(EVENTSTORE_CONFIG_GOSSIP_PORT, clusterConfig.getExternalGossipPort());
            mapped.put(EVENTSTORE_CONFIG_MAX_DISCOVER_ATTEMPTS,
                    clusterConfig.getMaxDiscoverAttempts());
            mapped.put(EVENTSTORE_CONFIG_DISCOVER_ATTEMPT_INTERVAL,
                    clusterConfig.getDiscoverAttemptInterval());
            mapped.put(EVENTSTORE_CONFIG_DISCOVERY_INTERVAL, clusterConfig.getDiscoveryInterval());
            mapped.put(EVENTSTORE_CONFIG_GOSSIP_TIMEOUT, clusterConfig.getGossipTimeout());
        }
        return mapped;
    }
}
