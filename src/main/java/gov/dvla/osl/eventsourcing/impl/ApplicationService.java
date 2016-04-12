package gov.dvla.osl.eventsourcing.impl;

import gov.dvla.osl.eventsourcing.api.Aggregate;
import gov.dvla.osl.eventsourcing.api.Command;
import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.api.ReflectionUtil;
import gov.dvla.osl.eventsourcing.store.httpeventstore.EventStoreWriter;
import gov.dvla.osl.eventsourcing.api.EventStore;
import gov.dvla.osl.eventsourcing.store.memory.EventStream;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

public class ApplicationService {
    private final EventStore eventStore;
    private EventStoreWriter eventStoreWriter;
    private String streamPrefix;
    private CommandHandlerLookup commandHandlerLookup;

    public ApplicationService(EventStore eventStore, EventStoreWriter eventStoreWriter, String streamPrefix, Class<?>... aggregateTypes) {
        this.eventStore = eventStore;
        this.eventStoreWriter = eventStoreWriter;
        this.streamPrefix = streamPrefix;
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
            eventStoreWriter.writeEvents(streamPrefix + "-" + command.aggregateId(), eventStream.version(), events);
//            eventStore.store(command.aggregateId(), eventStream.version(), events);
        }
    }

    private Object newAggregateInstance(Command command) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<?> clazz = commandHandlerLookup.targetType(command);
        Constructor<?> ctor = clazz.getConstructor(UUID.class);
        return ctor.newInstance(command.aggregateId());
    }
}
