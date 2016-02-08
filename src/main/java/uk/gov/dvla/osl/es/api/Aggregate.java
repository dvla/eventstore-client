package uk.gov.dvla.osl.es.api;

import java.util.List;
import java.util.UUID;

public interface Aggregate {
    List<Event> getUncommittedEvents();
    UUID aggregateId();
}
