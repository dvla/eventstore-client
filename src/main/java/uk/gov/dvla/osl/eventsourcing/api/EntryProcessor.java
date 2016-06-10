package uk.gov.dvla.osl.eventsourcing.api;

import uk.gov.dvla.osl.eventsourcing.store.http.entity.Entry;
import rx.Subscriber;

import java.util.List;

public interface EntryProcessor {
    void provideEntriesToSubscriber(final List<Entry> entries, final Subscriber subscriber);
}

