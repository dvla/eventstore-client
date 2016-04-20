package uk.gov.dvla.osl.memory;

import gov.dvla.osl.eventsourcing.store.memory.InMemoryEventStoreWriter;
import org.junit.Test;
import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.api.EventStream;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class InMemoryEventStoreReaderTest {
    UUID driverId = UUID.randomUUID();

    @Test
    public void test() throws Exception {
        InMemoryEventStoreWriter es = new InMemoryEventStoreWriter();
        es.store(driverId.toString(), 0, Arrays.asList(new SomeEvent(driverId, "forename", "surname", "email")));
        Thread.sleep(1);
        es.store(driverId.toString(), 1, Arrays.asList(new SomeEvent(driverId, "forename", "surname", "email")));
        EventStream<Long> stream = es.loadEventsAfter(0L);
        assertEquals(1, countEvents(stream));
        Long id = stream.version();
    }

    private int countEvents(EventStream<Long> stream) {
        int result = 0;
        for (Event event : stream) {
            result++;
        }
        return result;
    }
}

