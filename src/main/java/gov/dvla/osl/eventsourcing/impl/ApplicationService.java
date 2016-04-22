package gov.dvla.osl.eventsourcing.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.dvla.osl.eventsourcing.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

public class ApplicationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationService.class);

    private final EventStoreReader eventStoreReader;
    private EventStoreWriter eventStoreWriter;
    private String streamPrefix;
    private ObjectMapper mapper;
    private CommandHandlerLookup commandHandlerLookup;

    public ApplicationService(EventStoreReader eventStoreReader,
                              EventStoreWriter eventStoreWriter,
                              String streamPrefix,
                              ObjectMapper mapper,
                              Class<?>... aggregateTypes) {
        this.eventStoreReader = eventStoreReader;
        this.eventStoreWriter = eventStoreWriter;
        this.streamPrefix = streamPrefix;
        this.mapper = mapper;
        this.commandHandlerLookup = new CommandHandlerLookup(ReflectionUtil.HANDLE_METHOD, aggregateTypes);
    }

    public void handle(Command command) throws Exception {
        EventStream eventStream = eventStoreReader.loadEventStream(this.streamPrefix + "-" + command.aggregateId().toString());
        Object target = newAggregateInstance(command);
        for (Event event : eventStream) {
            LOGGER.debug("Applying event: " + event.getClass() + " " + mapper.writeValueAsString(event));
            ReflectionUtil.invokeHandleMethod(target, event);
        }
        LOGGER.debug("Invoking command: " + command.getClass() + " " + mapper.writeValueAsString(command));
        ReflectionUtil.invokeHandleMethod(target, command);
        List<Event> events = ((Aggregate) target).getUncommittedEvents();
        if (events != null && events.size() > 0) {
            eventStoreWriter.store(streamPrefix + "-" + command.aggregateId(), eventStream.version(), events);
        } else {
            LOGGER.debug("No events raised by aggregate");
        }
    }

    private Object newAggregateInstance(Command command) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Class<?> clazz = commandHandlerLookup.targetType(command);
        Constructor<?> ctor = clazz.getConstructor(UUID.class);
        LOGGER.debug("Creating aggregate: " + clazz);
        return ctor.newInstance(command.aggregateId());
    }
}
