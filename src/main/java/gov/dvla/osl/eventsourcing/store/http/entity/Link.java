package gov.dvla.osl.eventsourcing.store.http.entity;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "uri",
        "relation"
})
public class Link {

    @JsonProperty("uri")
    private String uri;
    @JsonProperty("relation")
    private String relation;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The uri
     */
    @JsonProperty("uri")
    public String getUri() {
        return uri;
    }

    /**
     *
     * @param uri
     * The uri
     */
    @JsonProperty("uri")
    public void setUri(String uri) {
        this.uri = uri;
    }

    /**
     *
     * @return
     * The relation
     */
    @JsonProperty("relation")
    public String getRelation() {
        return relation;
    }

    /**
     *
     * @param relation
     * The relation
     */
    @JsonProperty("relation")
    public void setRelation(String relation) {
        this.relation = relation;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
