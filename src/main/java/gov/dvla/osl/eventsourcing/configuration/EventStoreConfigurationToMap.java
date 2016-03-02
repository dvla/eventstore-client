package gov.dvla.osl.eventsourcing.configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration utility class.
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
    public static String EVENTSTORE_CONFIG_USERID = "eventstore.credentials.login";

    /**
     * Password.
     */
    public static String EVENTSTORE_CONFIG_PASSWORD = "eventstore.credentials.password";

    /**
     * Reconnection attempts
     */
    public static String EVENTSTORE_CONFIG_RECONNECTION_ATTEMPTS = "eventstore.max-reconnections";

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
        return mapped;
    }
}
