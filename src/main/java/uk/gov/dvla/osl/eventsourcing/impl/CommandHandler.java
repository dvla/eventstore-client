package uk.gov.dvla.osl.eventsourcing.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dvla.osl.eventsourcing.api.Aggregate;
import uk.gov.dvla.osl.eventsourcing.api.Command;
import uk.gov.dvla.osl.eventsourcing.api.Event;
import uk.gov.dvla.osl.eventsourcing.api.EventStoreReader;
import uk.gov.dvla.osl.eventsourcing.api.EventStoreWriter;
import uk.gov.dvla.osl.eventsourcing.api.EventStream;
import uk.gov.dvla.osl.eventsourcing.api.ReflectionUtil;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.UUID;

public class CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

    private final EventStoreReader eventStoreReader;
    private final EventStoreWriter eventStoreWriter;
    private final String streamPrefix;
    private final ObjectMapper mapper;
    private final CommandHandlerLookup commandHandlerLookup;

    public CommandHandler(final EventStoreReader eventStoreReader,
                          final EventStoreWriter eventStoreWriter,
                          final String streamPrefix,
                          final ObjectMapper mapper,
                          final Class<?>... aggregateTypes) {
        this.eventStoreReader = eventStoreReader;
        this.eventStoreWriter = eventStoreWriter;
        this.streamPrefix = streamPrefix;
        this.mapper = mapper;
        this.commandHandlerLookup = new CommandHandlerLookup(ReflectionUtil.HANDLE_METHOD, aggregateTypes);
    }

    public void handle(final Command command) throws Exception {
        final EventStream eventStream = eventStoreReader.loadEventStream(this.streamPrefix + "-" + command.aggregateId().toString());
        final Object target = newAggregateInstance(command);
        for (Event event : eventStream) {
            LOGGER.debug("Applying event: " + event.getClass() + " " + mapper.writeValueAsString(event));
            ReflectionUtil.invokeHandleMethod(target, event);
        }
        LOGGER.debug("Invoking command: " + command.getClass() + " " + mapper.writeValueAsString(command));
        ReflectionUtil.invokeHandleMethod(target, command);
        final List<Event> events = ((Aggregate) target).getUncommittedEvents();
        if (events != null && events.size() > 0) {
            eventStoreWriter.store(streamPrefix + "-" + command.aggregateId(), eventStream.version(), events);
        } else {
            LOGGER.debug("No events raised by aggregate");
        }
    }

    private Object newAggregateInstance(final Command command) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        final Class<?> clazz = commandHandlerLookup.targetType(command);
        final Constructor<?> ctor = clazz.getConstructor(UUID.class);
        LOGGER.debug("Creating aggregate: " + clazz);
        return ctor.newInstance(command.aggregateId());
    }
}
