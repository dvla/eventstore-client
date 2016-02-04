package uk.gov.dvla.osl.es.impl;

import uk.gov.dvla.osl.es.api.Command;
import uk.gov.dvla.osl.es.api.Event;
import uk.gov.dvla.osl.es.store.memory.EventStore;
import uk.gov.dvla.osl.es.store.memory.EventStream;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ApplicationService {

    private static final long DEFAULT_TIMEOUT_SECONDS = 1;
    private static final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.SECONDS;

    private final EventStore eventStore;
    private final long timeout;
    private final TimeUnit timeoutUnit;
    private CommandHandlerLookup commandHandlerLookup;

    public ApplicationService(EventStore eventStore, Class<?>... aggregateTypes) {
        this(eventStore, DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS, aggregateTypes);
    }

    public ApplicationService(EventStore eventStore, long timeout, final TimeUnit timeoutUnit, Class<?>... aggregateTypes) {
        this.eventStore = eventStore;
        this.timeout = timeout;
        this.timeoutUnit = timeoutUnit;
        this.commandHandlerLookup = new CommandHandlerLookup(ReflectionUtil.HANDLE_METHOD, aggregateTypes);
    }

    public void handle(Command command) throws Exception {
        EventStream<Long> eventStream = eventStore.loadEventStream(command.aggregateId());
        Object target = newAggregateInstance(command);
        for (Event event : eventStream) {
            ReflectionUtil.invokeHandleMethod(target, event);
        }
        List<Event> events = ReflectionUtil.invokeHandleMethod(target, command);
        if (events != null && events.size() > 0) {
            eventStore.storeBlocking(command.aggregateId(), eventStream.version(), events, timeout, timeoutUnit);
        } else {
            // Command generated no events
        }
    }

    private Object newAggregateInstance(Command command) throws InstantiationException, IllegalAccessException {
        return commandHandlerLookup.targetType(command).newInstance();
    }
}

