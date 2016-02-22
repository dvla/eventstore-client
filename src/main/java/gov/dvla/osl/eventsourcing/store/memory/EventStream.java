package gov.dvla.osl.eventsourcing.store.memory;


import gov.dvla.osl.eventsourcing.api.Event;

public interface EventStream<V> extends Iterable<Event> {
	V version();
}
