package gov.dvla.osl.eventsourcing.api;

import java.io.IOException;

public class EventStoreEvent {
    private String eventType;
    private String data;
    private int eventNumber;
    private String eventId;
    private int positionEventNumber;

    private EventDeserialiser eventDeserialiser;

    public EventStoreEvent(EventDeserialiser eventDeserialiser) {
        this.eventDeserialiser = eventDeserialiser;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Event getEvent() throws ClassNotFoundException, IOException {
        return eventDeserialiser.deserialise(this.data, this.eventType);
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
