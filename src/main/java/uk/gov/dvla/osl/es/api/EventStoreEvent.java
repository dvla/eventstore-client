package uk.gov.dvla.osl.es.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.util.UUID;

public class EventStoreEvent {
    private String eventType;
    private String data;
    private int eventNumber;
    private String eventId;
    private int positionEventNumber;

    private ObjectMapper mapper;

    public EventStoreEvent() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Event getEvent() throws ClassNotFoundException, IOException {
        Class clazz = Class.forName(this.eventType);
        return (Event) mapper.readValue(data, clazz);
    }

    public int getEventNumber() {
        return eventNumber;
    }

    public void setEventNumber(int eventNumber) {
        this.eventNumber = eventNumber;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
    * This returns the position of the event in the "parent" stream.  Think of subscribing to a category stream
     * or an event type stream here.  It is provided so you can save the position of the last successfully
     * processed event in that stream.
     */
    public int getPositionEventNumber() {
        return positionEventNumber;
    }

    public void setPositionEventNumber(int positionEventNumber) {
        this.positionEventNumber = positionEventNumber;
    }
}
