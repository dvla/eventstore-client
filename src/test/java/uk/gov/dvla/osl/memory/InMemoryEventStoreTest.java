package uk.gov.dvla.osl.memory;

import org.junit.Test;
import uk.gov.dvla.osl.es.api.Event;
import uk.gov.dvla.osl.es.store.memory.EventStream;
import uk.gov.dvla.osl.es.store.memory.InMemoryEventStore;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class InMemoryEventStoreTest {
    UUID driverId = UUID.randomUUID();

    @Test
    public void test() throws Exception {
        InMemoryEventStore es = new InMemoryEventStore();
        es.store(driverId, 0, Arrays.asList(new SomeEvent(driverId, "forename", "surname", "email")));
        Thread.sleep(1);
        es.store(driverId, 1, Arrays.asList(new SomeEvent(driverId, "forename", "surname", "email")));
        EventStream<Long> stream = es.loadEventsAfter(0L);
        assertEquals(1, countEvents(stream));
        Long id = stream.version();
        System.out.println("id=" + id);
    }

    private int countEvents(EventStream<Long> stream) {
        int result = 0;
        for (Event event : stream) {
            result++;
        }
        return result;
    }

}

