package uk.gov.dvla.osl.eventsourcing.api;

public class StreamPosition {

    /**
     * The first event in a stream.
     */
    public static final int START = 0;

    /**
     * The last event in the stream.
     */
    public static final int END = -1;

    private StreamPosition() {
    }
}