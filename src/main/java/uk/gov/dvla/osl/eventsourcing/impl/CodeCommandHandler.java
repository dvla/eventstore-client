package uk.gov.dvla.osl.eventsourcing.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dvla.osl.eventsourcing.api.Aggregate;
import uk.gov.dvla.osl.eventsourcing.api.Command;
import uk.gov.dvla.osl.eventsourcing.api.Event;
import uk.gov.dvla.osl.eventsourcing.api.EventDeserialiser;
import uk.gov.dvla.osl.eventsourcing.api.EventStoreReader;
import uk.gov.dvla.osl.eventsourcing.api.EventStoreWriter;
import uk.gov.dvla.osl.eventsourcing.api.ReflectionUtil;
import uk.gov.dvla.osl.eventsourcing.store.http.entity.Entry;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Deprecated in favour of CommandHandler.  This behaviour has been merged and a new constructor created on
 * CommandHandler that allows passing in of two functional arguments specifying a method to load existing events as
 * well as store newly created events.
 */
@Deprecated
public class CodeCommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CodeCommandHandler.class);

    private final EventStoreReader eventStoreReader;
    private final EventStoreWriter eventStoreWriter;
    private final String streamPrefix;
    private final ObjectMapper mapper;
    private final CommandHandlerLookup commandHandlerLookup;
    private final EventDeserialiser eventDeserialiser;

    public CodeCommandHandler(final EventStoreReader eventStoreReader,
                              final EventStoreWriter eventStoreWriter,
                              final String streamPrefix,
                              final ObjectMapper mapper,
                              final EventDeserialiser eventDeserialiser,
                              final Class<?>... aggregateTypes) {
        this.eventStoreReader = eventStoreReader;
        this.eventStoreWriter = eventStoreWriter;
        this.streamPrefix = streamPrefix;
        this.mapper = mapper;
        this.eventDeserialiser = eventDeserialiser;
        this.commandHandlerLookup = new CommandHandlerLookup(ReflectionUtil.HANDLE_METHOD, aggregateTypes);
    }

    public void handle(final Command command) throws Exception {
        handle(command, String.format("%s-%s", this.streamPrefix, command.aggregateId().toString()));
    }

    public void handle(final Command command, final String streamPrefix) throws Exception {

        final long[] lastEventNumber = {0};
        final List<Event> events = new ArrayList<>();

        eventStoreReader.readLastEvent(streamPrefix).subscribe(
                (Entry entry) -> {
                    lastEventNumber[0] = entry.getEventNumber();
                    Event event = this.eventDeserialiser.deserialise(entry.getData(), entry.getEventType());
                    events.add(event);
                },
                (error) -> LOGGER.error(error.getMessage(), error),
                () -> LOGGER.debug("Projection finished")
        );

        final Object target = newAggregateInstance(command);
        for (Event event : events) {
            LOGGER.debug("Applying event: " + event.getClass() + " " + mapper.writeValueAsString(event));
            ReflectionUtil.invokeHandleMethod(target, event);
        }
        LOGGER.debug("Invoking command: " + command.getClass() + " " + mapper.writeValueAsString(command));
        ReflectionUtil.invokeHandleMethod(target, command);
        final List<Event> newEvents = ((Aggregate) target).getUncommittedEvents();
        if (newEvents != null && newEvents.size() > 0) {
            eventStoreWriter.store(streamPrefix, events.size() > 0 ? lastEventNumber[0] : -1, newEvents);
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