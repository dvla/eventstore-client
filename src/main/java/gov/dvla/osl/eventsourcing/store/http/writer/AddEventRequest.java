package gov.dvla.osl.eventsourcing.store.http.writer;

import java.util.UUID;

public final class AddEventRequest {

    public final String eventId;
    public final String eventType;
    public final Object data;

    public AddEventRequest(final UUID eventId, final String eventType, final Object data) {
        this.eventId = eventId.toString();
        this.eventType = eventType;
        this.data = data;
    }
}