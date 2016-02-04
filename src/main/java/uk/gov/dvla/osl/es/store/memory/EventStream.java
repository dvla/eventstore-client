package uk.gov.dvla.osl.es.store.memory;


import uk.gov.dvla.osl.es.api.Event;

public interface EventStream<V> extends Iterable<Event> {
	V version();
}
