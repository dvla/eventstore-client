package uk.gov.dvla.osl.eventsourcing.store.memory;

import uk.gov.dvla.osl.eventsourcing.api.Event;
import uk.gov.dvla.osl.eventsourcing.api.EventStream;
import org.junit.Test;

import java.util.Arrays;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class InMemoryEventStoreReaderTest {
    UUID driverId = UUID.randomUUID();

    @Test
    public void test() throws Exception {
        InMemoryEventStore es = new InMemoryEventStore();
        es.store(driverId.toString(), 0, Arrays.asList(
                new SomeEvent(driverId, "forename", "surname", "email", new Date())));
        Thread.sleep(1);
        es.store(driverId.toString(), 1, Arrays.asList(
                new SomeEvent(driverId, "forename", "surname", "email", new Date())));
        EventStream stream = es.loadEventsAfter(0L);
        assertEquals(1, countEvents(stream));
        Long id = stream.version();
    }

    private int countEvents(EventStream stream) {
        int result = 0;
        for (Event event : stream) {
            result++;
        }
        return result;
    }
}
