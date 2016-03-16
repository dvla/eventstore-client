package gov.dvla.osl.eventsourcing.configuration;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration class for event store clustering parameters, as used by the eventstore JVM client.
 *
 * Settings defined in this relate to the settings defined in
 * https://github.com/EventStore/EventStore.JVM/blob/master/src/main/scala/eventstore/cluster/ClusterSettings.scala
 *
 * @since 1.0.0
 */
public class EventStoreClusterConfiguration {

    /**
     * Gossip seeds which compose the cluster, needed for connection to a cluster node.
     */
    @JsonProperty("gossip-seeds")
    private List<String> gossipSeeds;

    /**
     * Dns entry containing all the nodes, not required.
     * http://docs.geteventstore.com/server/3.5.0/cluster-without-manager-nodes/
     */
    @JsonProperty("dns")
    private String dns;

    /**
     * Timeout to apply when doing a dns lookup.
     */
    @JsonProperty("dns-lookup-timeout")
    private String dnsLookupTimeout;

    /**
     * External gossip port for the cluster.
     */
    @JsonProperty("external-gossip-port")
    private Integer externalGossipPort;

    /**
     * Maximum number of attempts for discovering endpoints.
     */
    @JsonProperty("max-discover-attempts")
    private Integer maxDiscoverAttempts;

    /**
     * Interval between discovery attempts.
     */
    @JsonProperty("discover-attempt-interval")
    private String discoverAttemptInterval;

    /**
     * Interval at which to keep discovering cluster.
     */
    @JsonProperty("discovery-interval")
    private String discoveryInterval;

    /**
     * Timeout for cluster gossip.
     */
    @JsonProperty("gossip-timeout")
    private String gossipTimeout;

    /**
     * Return the gossip seeds for the cluster.
     * Format for each seed is "hostip:port"
     * @return List of gossip seeds for cluster.
     */
    public List<String> getGossipSeeds() {
        return gossipSeeds;
    }

    /**
     * Set the gossip seeds for the cluster.
     * Format for each seed is "hostip:port"
     * @param gossipSeeds - the list of seeds
     */
    public void setGossipSeeds(final List<String> gossipSeeds) {
        this.gossipSeeds = gossipSeeds;
    }

    /**
     * Dns hostname to use for the cluster, where the dns-based approach is being used.
     * @return the dns hostname
     */
    public String getDns() {
        return dns;
    }

    /**
     * Set the dns hostname for the cluster, where the dns-based approach is being used.
     * @param dns - the dns hostname
     */
    public void setDns(final String dns) {
        this.dns = dns;
    }

    /**
     * Get the dns lookup timeout.
     * @return the dns lookup timeout
     */
    public String getDnsLookupTimeout() {
        return dnsLookupTimeout;
    }

    /**
     * Set the dns lookup timeout value.
     * @param dnsLookupTimeout - new lookup timeout value
     */
    public void setDnsLookupTimeout(final String dnsLookupTimeout) {
        this.dnsLookupTimeout = dnsLookupTimeout;
    }

    /**
     * Get the external gossip port.
     * @return the external gossip port.
     */
    public Integer getExternalGossipPort() {
        return externalGossipPort;
    }

    /**
     * Set the external gossip port.
     * @param externalGossipPort - the new external gossip port.
     */
    public void setExternalGossipPort(final Integer externalGossipPort) {
        this.externalGossipPort = externalGossipPort;
    }

    /**
     * Get the maximum discovery attempts.
     * @return maximum discovery attempts before aborting
     */
    public Integer getMaxDiscoverAttempts() {
        return maxDiscoverAttempts;
    }

    /**
     * Set the maximum number of discovery attempts.
     * @param maxDiscoverAttempts - the new limit on discovery attempts
     */
    public void setMaxDiscoverAttempts(final Integer maxDiscoverAttempts) {
        this.maxDiscoverAttempts = maxDiscoverAttempts;
    }

    /**
     * Get the discovery attempt interval.
     * @return the interval between discovery attempts
     */
    public String getDiscoverAttemptInterval() {
        return discoverAttemptInterval;
    }

    /**
     * Set the interval between discovery attempts.
     * @param discoverAttemptInterval - the new discovery attempt interval
     */
    public void setDiscoverAttemptInterval(final String discoverAttemptInterval) {
        this.discoverAttemptInterval = discoverAttemptInterval;
    }

    /**
     * Get the interval at which to keep discovering the cluster.
     * @return the discovery interval
     */
    public String getDiscoveryInterval() {
        return discoveryInterval;
    }

    /**
     * Set the interval at which to keep discovering the cluster.
     * @param discoveryInterval - the new discovery interval
     */
    public void setDiscoveryInterval(final String discoveryInterval) {
        this.discoveryInterval = discoveryInterval;
    }

    /**
     * Get the gossip timeout.
     * @return the gossip timeout
     */
    public String getGossipTimeout() {
        return gossipTimeout;
    }

    /**
     * Set the gossip timeout.
     * @param gossipTimeout - the new gossip timeout
     */
    public void setGossipTimeout(final String gossipTimeout) {
        this.gossipTimeout = gossipTimeout;
    }

}
