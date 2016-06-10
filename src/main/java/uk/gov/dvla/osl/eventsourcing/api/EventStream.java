package uk.gov.dvla.osl.eventsourcing.api;

import uk.gov.dvla.osl.eventsourcing.api.Event;

public interface EventStream extends Iterable<Event> {
	Long version();
}

