package uk.gov.dvla.osl.eventsourcing.projection;

import uk.gov.dvla.osl.eventsourcing.api.Event;

public class ProjectedEvent {

    private Event event;
    private Integer positionEventNumber;

    public ProjectedEvent(Event event, Integer positionEventNumber) {
        this.event = event;
        this.positionEventNumber = positionEventNumber;
    }

    public Event getEvent() {
        return event;
    }

    public Integer getPositionEventNumber() {
        return positionEventNumber;
    }
}
