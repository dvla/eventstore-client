package uk.gov.dvla.osl.memory;

import org.junit.Test;
import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.api.EventStream;
import gov.dvla.osl.eventsourcing.store.memory.InMemoryEventStoreReader;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class InMemoryEventStoreReaderTest {
    UUID driverId = UUID.randomUUID();

    @Test
    public void test() throws Exception {
        InMemoryEventStoreReader es = new InMemoryEventStoreReader();
        es.store(driverId, 0, Arrays.asList(new SomeEvent(driverId, "forename", "surname", "email")));
        Thread.sleep(1);
        es.store(driverId, 1, Arrays.asList(new SomeEvent(driverId, "forename", "surname", "email")));
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

