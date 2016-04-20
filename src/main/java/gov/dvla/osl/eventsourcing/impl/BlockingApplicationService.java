package gov.dvla.osl.eventsourcing.impl;

import gov.dvla.osl.eventsourcing.api.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

public class BlockingApplicationService {

    private final EventStoreReader eventStoreReader;
    private EventStoreWriter eventStoreWriter;
    private CommandHandlerLookup commandHandlerLookup;

    public BlockingApplicationService(EventStoreReader eventStoreReader, EventStoreWriter eventStoreWriter, Class<?>... aggregateTypes) {
        this.eventStoreReader = eventStoreReader;
        this.eventStoreWriter = eventStoreWriter;
        this.commandHandlerLookup = new CommandHandlerLookup(ReflectionUtil.HANDLE_METHOD, aggregateTypes);
    }

    public void handle(Command command) throws Exception {
        EventStream<Long> eventStream = eventStoreReader.loadEventStream(command.aggregateId());
        Object target = newAggregateInstance(command);
        for (Event event : eventStream) {
            ReflectionUtil.invokeHandleMethod(target, event);
        }
        ReflectionUtil.invokeHandleMethod(target, command);
        List<Event> events = ((Aggregate) target).getUncommittedEvents();
        if (events != null && events.size() > 0) {
            eventStoreWriter.store(command.aggregateId().toString(), eventStream.version(), events);
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