package gov.dvla.osl.eventsourcing.api;

public interface EventStream<V> extends Iterable<Event> {
	V version();
}
