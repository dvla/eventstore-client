package gov.dvla.osl.eventsourcing.store.httpeventstore.entity;

import com.fasterxml.jackson.annotation.*;
import javax.annotation.Generated;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "title",
        "id",
        "updated",
        "streamId",
        "author",
        "headOfStream",
        "links",
        "entries"
})
public class EventStreamData {

    @JsonProperty("title")
    private String title;
    @JsonProperty("id")
    private String id;
    @JsonProperty("updated")
    private String updated;
    @JsonProperty("streamId")
    private String streamId;
    @JsonProperty("author")
    private Author author;
    @JsonProperty("headOfStream")
    private Boolean headOfStream;
    @JsonProperty("links")
    private List<Link> links = new ArrayList<Link>();
    @JsonProperty("entries")
    private List<Entry> entries = new ArrayList<Entry>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The title
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     * The title
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     * The id
     */
    @JsonProperty("id")
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The updated
     */
    @JsonProperty("updated")
    public String getUpdated() {
        return updated;
    }

    /**
     *
     * @param updated
     * The updated
     */
    @JsonProperty("updated")
    public void setUpdated(String updated) {
        this.updated = updated;
    }

    /**
     *
     * @return
     * The streamId
     */
    @JsonProperty("streamId")
    public String getStreamId() {
        return streamId;
    }

    /**
     *
     * @param streamId
     * The streamId
     */
    @JsonProperty("streamId")
    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    /**
     *
     * @return
     * The author
     */
    @JsonProperty("author")
    public Author getAuthor() {
        return author;
    }

    /**
     *
     * @param author
     * The author
     */
    @JsonProperty("author")
    public void setAuthor(Author author) {
        this.author = author;
    }

    /**
     *
     * @return
     * The headOfStream
     */
    @JsonProperty("headOfStream")
    public Boolean getHeadOfStream() {
        return headOfStream;
    }

    /**
     *
     * @param headOfStream
     * The headOfStream
     */
    @JsonProperty("headOfStream")
    public void setHeadOfStream(Boolean headOfStream) {
        this.headOfStream = headOfStream;
    }

    /**
     *
     * @return
     * The links
     */
    @JsonProperty("links")
    public List<Link> getLinks() {
        return links;
    }

    /**
     *
     * @param links
     * The links
     */
    @JsonProperty("links")
    public void setLinks(List<Link> links) {
        this.links = links;
    }

    /**
     *
     * @return
     * The entries
     */
    @JsonProperty("entries")
    public List<Entry> getEntries() {
        return entries;
    }

    /**
     *
     * @param entries
     * The entries
     */
    @JsonProperty("entries")
    public void setEntries(List<Entry> entries) {
        this.entries = entries;
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
