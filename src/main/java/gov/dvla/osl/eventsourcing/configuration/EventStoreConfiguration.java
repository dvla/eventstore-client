package gov.dvla.osl.eventsourcing.configuration;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.concurrent.TimeUnit;

/**
 * The event store configuration.
 *
 * A wrapper so we can inject dropwizard configuration for eventstore into akka configuration (we
 * really want the event store connection parameters to be defined in the dropwizard configuration).
 * This can be extended to include other eventstore parameters later.
 *
 */
public class EventStoreConfiguration {

    /**
     * Maximum value for port.
     */
    private static final int MAX_PORT = 65535;

    /**
     * Default value for port.
     */
    private static final int DEFAULT_PORT = 1113;
    private static final int DEFAULT_HTTP_PORT = 2113;

    /**
     * Maximum number for the reconnect attempts.
     */
    private static final int MAX_RECONNECT_ATTEMPTS = 10000;

    /**
     * Default value for the reconnect attempts value.
     */
    private static final int DEFAULT_RECONNECT_ATTEMPTS = 1000;

    /**
     * Default value for the store timeout
     */
    private static final long DEFAULT_TIMEOUT_SECONDS = 1;

    /**
     * The scheme.
     */
    @NotEmpty
    @JsonProperty
    private String scheme = "http";

    /**
     * The host.
     */
    @NotEmpty
    @JsonProperty
    private String host;

    /**
     * The port.
     */
    @Min(1)
    @Max(MAX_PORT)
    @JsonProperty
    private int port = DEFAULT_PORT;

    @Min(1)
    @Max(MAX_PORT)
    @JsonProperty
    private int httpPort = DEFAULT_HTTP_PORT;

    /**
     * The user id.
     */
    @NotEmpty
    @JsonProperty
    private String userId;

    /**
     * The password.
     */
    @NotEmpty
    @JsonProperty
    private String password;

    /**
     * Maximum number of reconnection attempts for the eventstore client before it backs out.
     * reconnecting following connection loss
     */
    @Min(-1)
    @Max(MAX_RECONNECT_ATTEMPTS)
    @JsonProperty
    private int reconnectionAttempts = DEFAULT_RECONNECT_ATTEMPTS;

    /**
     * Timeout for the storing of events
     */
    @Min(1)
    @JsonProperty
    private long timeoutSeconds = DEFAULT_TIMEOUT_SECONDS;

    /**
     * Projection configuration.
     */
    @JsonProperty
    private ProjectionConfiguration projection;

    /**
     * The health check URL.
     */
    @JsonProperty
    private String healthCheckUrl;

    /**
     * Cluster configuration (optional but needed if we want to talk to an eventstore cluster).
     */
    @JsonProperty
    private EventStoreClusterConfiguration cluster;

    /**
     * Constructor.
     */
    public EventStoreConfiguration() {
        // Noop for Dropwizard config
    }

    /**
     * Constructor.
     * @param host the host
     * @param port the port
     * @param userId the user id
     * @param password the password
     */
    public EventStoreConfiguration(final String scheme, final String host, final int port, final String userId,
                                   final String password) {
        this.scheme = scheme;
        this.host = host;
        this.port = port;
        this.userId = userId;
        this.password = password;
    }

    /**
     * Get scheme.
     * @return scheme
     */
    public String getScheme() {
        return scheme;
    }

    /**
     * Get host.
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * Get port.
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * Get httpPort.
     * @return httpPort
     */
    public int getHttpPort() {
        return httpPort;
    }

    /**
     * Get user id.
     * @return userId
     */
    public String getUserId() {
        return userId;
    }

    /**
     * Get password.
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get the number of seconds before timing out an eventstore write
     * @return max reconnection attempts
     */
    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }

    /**
     * Get maximum number of reconnection attempts to make to an eventstore server in the event
     * of a connection loss.
     * @return max reconnection attempts
     */
    public int getReconnectionAttempts() {
        return reconnectionAttempts;
    }

    /**
     * Retrieve the projection configuration.
     * @return the projection configuration
     */
    public ProjectionConfiguration getProjectionConfiguration() {
        return projection;
    }

    /**
     * Get health check URL.
     * @return health check URL
     */
    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    /**
     * Return the eventstore cluster configuration.
     * @return eventstore cluster configuration
     */
    public EventStoreClusterConfiguration getCluster() {
        return cluster;
    }

    /**
     * Set the eventstore cluster configuration.
     * @param cluster - the new cluster configuration
     */
    public void setCluster(EventStoreClusterConfiguration cluster) {
        this.cluster = cluster;
    }
}
