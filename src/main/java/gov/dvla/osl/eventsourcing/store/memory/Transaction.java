package gov.dvla.osl.eventsourcing.store.memory;

import gov.dvla.osl.eventsourcing.api.Event;

import java.util.Collections;
import java.util.List;

class Transaction implements Comparable<Transaction> {
    public final List<? extends Event> events;
    private final long timestamp;

    public Transaction(final long timestamp) {
        events = Collections.emptyList();
        this.timestamp = timestamp;

    }
    public Transaction(final List<? extends Event> events) {
        this.events = events;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public int compareTo(final Transaction other) {
        if (timestamp < other.timestamp) {
            return -1;
        } else if (timestamp > other.timestamp) {
            return 1;
        }
        return 0;
    }
}