package gov.dvla.osl.eventsourcing.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * The event store projection configuration.
 *
 */
public class ProjectionConfiguration {

    /**
     * The pageSize.
     */
    @Min(1)
    @Max(50)
    @JsonProperty
    private int pageSize = 20;

    /**
     * The retry interval in seconds.
     */
    @Min(1)
    @Max(60)
    @JsonProperty
    private int secondsBeforeRetry = 30;

    /**
     * The stream.
     */
    @NotEmpty
    @JsonProperty
    private String stream;

    @JsonProperty
    private boolean keepAlive;

    /**
     * Constructor.
     */
    public ProjectionConfiguration() {
        // Noop for Dropwizard config
    }

    /**
     * Constructor.
     * @param host the host
     * @param port the port
     * @param userId the user id
     * @param password the password
     */
//    public ProjectionConfiguration(final String host, final int port, final String stream, final String healthcheckUrl, final String userId, final String password) {
//        this.stream = stream;
//    }

    /**
     * Get stream.
     * @return stream
     */
    public String getStream() {
        return this.stream;
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

    public int getSecondsBeforeRetry() {
        return secondsBeforeRetry;
    }

    public void setSecondsBeforeRetry(int secondsBeforeRetry) {
        this.secondsBeforeRetry = secondsBeforeRetry;
    }
}
