package gov.dvla.osl.eventsourcing.api;

public interface EventStream extends Iterable<Event> {
	Long version();
}

