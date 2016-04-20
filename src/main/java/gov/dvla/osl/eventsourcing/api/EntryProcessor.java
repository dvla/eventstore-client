package gov.dvla.osl.eventsourcing.api;

import gov.dvla.osl.eventsourcing.store.http.entity.Entry;
import rx.Subscriber;

import java.util.List;

public interface EntryProcessor {
    void provideEntriesToSubscriber(List<Entry> entries, Subscriber subscriber);
}

