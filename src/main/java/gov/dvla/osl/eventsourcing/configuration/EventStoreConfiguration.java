package gov.dvla.osl.eventsourcing.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * The event store configuration.
 *
 * A wrapper so we can inject dropwizard configuration for eventstore into akka configuration (we really want the
 * event store connection parameters to be defined in the dropwizard configuration). This can be extended to include
 * other eventstore parameters later.
 *
 */
public class EventStoreConfiguration {
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
    @Max(65535)
    @JsonProperty
    private int port = 1113;

    /**
     * The pageSize.
     */
    @Min(1)
    @Max(50)
    @JsonProperty
    private int pageSize = 20;

    /**
     * The stream.
     */
    @NotEmpty
    @JsonProperty
    private String stream;

    /**
     * The health check url.
     */
    @NotEmpty
    @JsonProperty
    private String healthcheckUrl;

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
     * Maxinum number of reconnection attempts for the eventstore client before it backs out
     * reconnecting following connection loss
     */
    @Min(-1)
    @Max(10000)
    private int reconnectionAttempts = 1000;

    @JsonProperty
    private boolean keepAlive;

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
    public EventStoreConfiguration(final String host, final int port, final String stream, final String healthcheckUrl, final String userId, final String password) {
        this.host = host;
        this.port = port;
        this.stream = stream;
        this.healthcheckUrl = healthcheckUrl;
        this.userId = userId;
        this.password = password;
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
     * Get stream.
     * @return stream
     */
    public String getStream() {
        return this.stream;
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
     * Get maximum number of reconnection attempts to make to an eventstore server in the event
     * of a connection loss
     * @return max reconnection attempts
     */
    public int getReconnectionAttempts() {
        return reconnectionAttempts;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getHealthcheckUrl() {
        return healthcheckUrl;
    }

    public void setHealthcheckUrl(String healthcheckUrl) {
        this.healthcheckUrl = healthcheckUrl;
    }
}
