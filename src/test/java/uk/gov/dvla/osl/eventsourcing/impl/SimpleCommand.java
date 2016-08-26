package uk.gov.dvla.osl.eventsourcing.impl;

import uk.gov.dvla.osl.eventsourcing.api.Command;

import java.util.UUID;

public class SimpleCommand implements Command {

    private UUID id;

    public SimpleCommand(UUID id) {
        this.id = id;
    }

    @Override
    public UUID aggregateId() {
        return id;
    }
}