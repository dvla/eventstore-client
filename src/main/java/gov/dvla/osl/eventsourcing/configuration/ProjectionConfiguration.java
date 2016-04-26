package gov.dvla.osl.eventsourcing.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

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
    @JsonProperty
    private String stream;

    @JsonProperty
    private boolean keepAlive;

    /**
     * The long poll length in seconds.
     */
    @Min(1)
    @Max(60)
    @JsonProperty
    private String longPollSeconds = "30";

    /**
     * Constructor.
     */
    public ProjectionConfiguration() {
        // Noop for Dropwizard config
    }

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

    public String getLongPollSeconds() {
        return longPollSeconds;
    }
}
