package gov.dvla.osl.eventsourcing.store.httpeventstore;

import java.util.UUID;

public final class AddEventRequest {

    public final String eventId;
    public final String eventType;
    public final String data;

    public AddEventRequest(final UUID eventId, final String eventType, final String data) {
        this.eventId = eventId.toString();
        this.eventType = eventType;
        this.data = data;
    }
}