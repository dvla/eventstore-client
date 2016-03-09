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
        "eventId",
        "eventType",
        "eventNumber",
        "data",
        "metaData",
        "linkMetaData",
        "streamId",
        "isJson",
        "isMetaData",
        "isLinkMetaData",
        "positionEventNumber",
        "positionStreamId",
        "title",
        "id",
        "updated",
        "author",
        "summary",
        "links"
})
public class Entry {

    @JsonProperty("eventId")
    private String eventId;
    @JsonProperty("eventType")
    private String eventType;
    @JsonProperty("eventNumber")
    private Integer eventNumber;
    @JsonProperty("data")
    private String data;
    @JsonProperty("metaData")
    private String metaData;
    @JsonProperty("linkMetaData")
    private String linkMetaData;
    @JsonProperty("streamId")
    private String streamId;
    @JsonProperty("isJson")
    private Boolean isJson;
    @JsonProperty("isMetaData")
    private Boolean isMetaData;
    @JsonProperty("isLinkMetaData")
    private Boolean isLinkMetaData;
    @JsonProperty("positionEventNumber")
    private Integer positionEventNumber;
    @JsonProperty("positionStreamId")
    private String positionStreamId;
    @JsonProperty("title")
    private String title;
    @JsonProperty("id")
    private String id;
    @JsonProperty("updated")
    private String updated;
    @JsonProperty("author")
    private Author author;
    @JsonProperty("summary")
    private String summary;
    @JsonProperty("links")
    private List<Link> links = new ArrayList<Link>();
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     *
     * @return
     * The eventId
     */
    @JsonProperty("eventId")
    public String getEventId() {
        return eventId;
    }

    /**
     *
     * @param eventId
     * The eventId
     */
    @JsonProperty("eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     *
     * @return
     * The eventType
     */
    @JsonProperty("eventType")
    public String getEventType() {
        return eventType;
    }

    /**
     *
     * @param eventType
     * The eventType
     */
    @JsonProperty("eventType")
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     *
     * @return
     * The eventNumber
     */
    @JsonProperty("eventNumber")
    public Integer getEventNumber() {
        return eventNumber;
    }

    /**
     *
     * @param eventNumber
     * The eventNumber
     */
    @JsonProperty("eventNumber")
    public void setEventNumber(Integer eventNumber) {
        this.eventNumber = eventNumber;
    }

    /**
     *
     * @return
     * The data
     */
    @JsonProperty("data")
    public String getData() {
        return data;
    }

    /**
     *
     * @param data
     * The data
     */
    @JsonProperty("data")
    public void setData(String data) {
        this.data = data;
    }

    /**
     *
     * @return
     * The metaData
     */
    @JsonProperty("metaData")
    public String getMetaData() {
        return metaData;
    }

    /**
     *
     * @param metaData
     * The metaData
     */
    @JsonProperty("metaData")
    public void setMetaData(String metaData) {
        this.metaData = metaData;
    }

    /**
     *
     * @return
     * The linkMetaData
     */
    @JsonProperty("linkMetaData")
    public String getLinkMetaData() {
        return linkMetaData;
    }

    /**
     *
     * @param linkMetaData
     * The linkMetaData
     */
    @JsonProperty("linkMetaData")
    public void setLinkMetaData(String linkMetaData) {
        this.linkMetaData = linkMetaData;
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
     * The isJson
     */
    @JsonProperty("isJson")
    public Boolean getIsJson() {
        return isJson;
    }

    /**
     *
     * @param isJson
     * The isJson
     */
    @JsonProperty("isJson")
    public void setIsJson(Boolean isJson) {
        this.isJson = isJson;
    }

    /**
     *
     * @return
     * The isMetaData
     */
    @JsonProperty("isMetaData")
    public Boolean getIsMetaData() {
        return isMetaData;
    }

    /**
     *
     * @param isMetaData
     * The isMetaData
     */
    @JsonProperty("isMetaData")
    public void setIsMetaData(Boolean isMetaData) {
        this.isMetaData = isMetaData;
    }

    /**
     *
     * @return
     * The isLinkMetaData
     */
    @JsonProperty("isLinkMetaData")
    public Boolean getIsLinkMetaData() {
        return isLinkMetaData;
    }

    /**
     *
     * @param isLinkMetaData
     * The isLinkMetaData
     */
    @JsonProperty("isLinkMetaData")
    public void setIsLinkMetaData(Boolean isLinkMetaData) {
        this.isLinkMetaData = isLinkMetaData;
    }

    /**
     *
     * @return
     * The positionEventNumber
     */
    @JsonProperty("positionEventNumber")
    public Integer getPositionEventNumber() {
        return positionEventNumber;
    }

    /**
     *
     * @param positionEventNumber
     * The positionEventNumber
     */
    @JsonProperty("positionEventNumber")
    public void setPositionEventNumber(Integer positionEventNumber) {
        this.positionEventNumber = positionEventNumber;
    }

    /**
     *
     * @return
     * The positionStreamId
     */
    @JsonProperty("positionStreamId")
    public String getPositionStreamId() {
        return positionStreamId;
    }

    /**
     *
     * @param positionStreamId
     * The positionStreamId
     */
    @JsonProperty("positionStreamId")
    public void setPositionStreamId(String positionStreamId) {
        this.positionStreamId = positionStreamId;
    }

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
     * The summary
     */
    @JsonProperty("summary")
    public String getSummary() {
        return summary;
    }

    /**
     *
     * @param summary
     * The summary
     */
    @JsonProperty("summary")
    public void setSummary(String summary) {
        this.summary = summary;
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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
