package uk.gov.dvla.osl.eventsourcing.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.functions.Action3;
import rx.functions.Func1;
import uk.gov.dvla.osl.eventsourcing.api.Aggregate;
import uk.gov.dvla.osl.eventsourcing.api.Command;
import uk.gov.dvla.osl.eventsourcing.api.Event;
import uk.gov.dvla.osl.eventsourcing.api.EventStoreReader;
import uk.gov.dvla.osl.eventsourcing.api.EventStoreWriter;
import uk.gov.dvla.osl.eventsourcing.api.EventStream;
import uk.gov.dvla.osl.eventsourcing.api.ReflectionUtil;

import java.util.List;

public class CommandHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandler.class);

    private final String streamPrefix;
    private Func1<String, EventStream> loader;
    private Action3<String, Long, List<Event>> writer;
    private final CommandHandlerLookup commandHandlerLookup;

    @Deprecated
    public CommandHandler(final EventStoreReader eventStoreReader,
                          final EventStoreWriter eventStoreWriter,
                          final String streamPrefix,
                          final ObjectMapper mapper,
                          final Class<?>... aggregateTypes) {
        this(streamPrefix, eventStoreReader::loadEventStream, eventStoreWriter::store, aggregateTypes);
    }

    public CommandHandler(final String streamPrefix,
                          final Func1<String, EventStream> loader,
                          final Action3<String, Long, List<Event>> writer,
                          final Class<?>... aggregateTypes) {
        this(streamPrefix, loader, writer, new CommandHandlerLookup(ReflectionUtil.HANDLE_METHOD, aggregateTypes));
    }

    public CommandHandler(final String streamPrefix,
                          final Func1<String, EventStream> loader,
                          final Action3<String, Long, List<Event>> writer,
                          CommandHandlerLookup commandHandlerLookup) {
        this.streamPrefix = streamPrefix;
        this.loader = loader;
        this.writer = writer;
        this.commandHandlerLookup = commandHandlerLookup;
    }

    public void handle(final Command command) throws Exception {
        String streamName = String.format("%s-%s", this.streamPrefix, command.aggregateId().toString());
        handle(command, streamName);
    }

    public void handle(final Command command,
                       final String streamName) throws Exception {

        final EventStream eventStream = loader.call(streamName);
        final Object target = commandHandlerLookup.newAggregateInstance(command);
        for (Event event : eventStream) {
            ReflectionUtil.invokeHandleMethod(target, event);
        }
        ReflectionUtil.invokeHandleMethod(target, command);
        final List<Event> events = ((Aggregate) target).getUncommittedEvents();
        if (events != null && events.size() > 0) {
            writer.call(streamName, eventStream.version(), events);
        } else {
            LOGGER.debug("No events raised by aggregate");
        }
    }
}
