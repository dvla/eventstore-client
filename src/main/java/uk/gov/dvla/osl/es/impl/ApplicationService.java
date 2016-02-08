package uk.gov.dvla.osl.es.impl;

import uk.gov.dvla.osl.es.api.Aggregate;
import uk.gov.dvla.osl.es.api.Command;
import uk.gov.dvla.osl.es.api.Event;
import uk.gov.dvla.osl.es.store.memory.EventStore;
import uk.gov.dvla.osl.es.store.memory.EventStream;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

public class ApplicationService {
    private final EventStore eventStore;
    private CommandHandlerLookup commandHandlerLookup;

    public ApplicationService(EventStore eventStore, Class<?>... aggregateTypes) {
        this.eventStore = eventStore;
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
            eventStore.store(command.aggregateId(), eventStream.version(), events);
        }
    }

    private Object newAggregateInstance(Command command) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<?> clazz = commandHandlerLookup.targetType(command);
        Constructor<?> ctor = clazz.getConstructor(UUID.class);
        return ctor.newInstance(command.aggregateId());
    }
}

