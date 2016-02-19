package gov.dvla.osl.eventsourcing.impl;

import gov.dvla.osl.eventsourcing.api.Aggregate;
import gov.dvla.osl.eventsourcing.api.Command;
import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.store.memory.EventStore;
import gov.dvla.osl.eventsourcing.store.memory.EventStream;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BlockingApplicationService {

    private static final long DEFAULT_TIMEOUT_SECONDS = 1;
    private static final TimeUnit DEFAULT_TIMEUNIT = TimeUnit.SECONDS;

    private final EventStore eventStore;
    private final long timeout;
    private final TimeUnit timeoutUnit;
    private CommandHandlerLookup commandHandlerLookup;

    public BlockingApplicationService(EventStore eventStore, Class<?>... aggregateTypes) {
        this(eventStore, DEFAULT_TIMEOUT_SECONDS, TimeUnit.SECONDS, aggregateTypes);
    }

    public BlockingApplicationService(EventStore eventStore, long timeout, final TimeUnit timeoutUnit, Class<?>... aggregateTypes) {
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
        ReflectionUtil.invokeHandleMethod(target, command);
        List<Event> events = ((Aggregate) target).getUncommittedEvents();
        if (events != null && events.size() > 0) {
            eventStore.storeBlocking(command.aggregateId(), eventStream.version(), events, timeout, timeoutUnit);
        } else {
            // Command generated no events
        }
    }

    private Object newAggregateInstance(Command command) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<?> clazz = commandHandlerLookup.targetType(command);
        Constructor<?> ctor = clazz.getConstructor(UUID.class);
        return ctor.newInstance(command.aggregateId());
    }
}