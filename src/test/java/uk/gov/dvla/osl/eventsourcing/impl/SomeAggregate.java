package uk.gov.dvla.osl.eventsourcing.impl;

import uk.gov.dvla.osl.eventsourcing.api.Aggregate;
import uk.gov.dvla.osl.eventsourcing.api.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SomeAggregate implements Aggregate {

    private UUID id;
    private List<Event> uncommittedEvents = new ArrayList<>();

    public SomeAggregate(UUID id) {
        this.id = id;
    }

    @Override
    public List<Event> getUncommittedEvents() {
        return uncommittedEvents;
    }

    public void handle(SimpleCommand command) {
        uncommittedEvents.add(new SimpleEvent(command.aggregateId()));
    }

    public void handle(AnotherCommand command) {
    }

    @Override
    public UUID aggregateId() {
        return id;
    }
}