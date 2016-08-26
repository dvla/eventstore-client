package uk.gov.dvla.osl.eventsourcing.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import uk.gov.dvla.osl.eventsourcing.api.Command;
import uk.gov.dvla.osl.eventsourcing.api.Event;
import uk.gov.dvla.osl.eventsourcing.api.EventStoreReader;
import uk.gov.dvla.osl.eventsourcing.api.EventStoreWriter;
import uk.gov.dvla.osl.eventsourcing.store.memory.ListEventStream;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.MatcherAssert.assertThat;

public class CommandHandlerTest {

    private UUID aggregateId = UUID.randomUUID();

    @Test
    public void CommandHandlerLoadsExistingEventsForCorrectStreamWhenStreamPrefixPassedInAsConstructorParameter() throws Exception {

        // Arrange
        //
        String expectedStreamName = "streamprefix-" + aggregateId.toString();
        List<Event> events = new ArrayList<>();
        events.add(new SimpleEvent(aggregateId));

        EventStoreReader eventStoreReader = mock(EventStoreReader.class);
        when(eventStoreReader.loadEventStream(expectedStreamName)).thenReturn(new ListEventStream(0, events));

        EventStoreWriter eventStoreWriter = mock(EventStoreWriter.class);

        CommandHandler commandHandler = new CommandHandler("streamprefix",
                eventStoreReader::loadEventStream,
                eventStoreWriter::store,
                SomeAggregate.class);

        // Act
        //
        commandHandler.handle(new SimpleCommand(aggregateId));

        // Assert
        //
        verify(eventStoreReader).loadEventStream(expectedStreamName);
    }

    @Test
    public void EventStoreWriterStoresNewEventsToCorrectStreamWhenStreamPrefixPassedInAsConstructorParameter() throws Exception {

        // Arrange
        //
        String expectedStreamName = "streamprefix-" + aggregateId.toString();
        List<Event> events = new ArrayList<>();
        events.add(new SimpleEvent(aggregateId));

        EventStoreReader eventStoreReader = mock(EventStoreReader.class);
        when(eventStoreReader.loadEventStream(expectedStreamName)).thenReturn(new ListEventStream(0, events));

        EventStoreWriter eventStoreWriter = mock(EventStoreWriter.class);
        ArgumentCaptor<List> capturedEvents = ArgumentCaptor.forClass(List.class);

        CommandHandler commandHandler = new CommandHandler("streamprefix",
                eventStoreReader::loadEventStream,
                eventStoreWriter::store,
                SomeAggregate.class);

        // Act
        //
        commandHandler.handle(new SimpleCommand(aggregateId));

        // Assert
        //
        verify(eventStoreWriter).store(eq(expectedStreamName), anyLong(), capturedEvents.capture());

        // check that the one event raised by the aggregate was passed to the writer
        assertThat("Expected 1 event", capturedEvents.getAllValues().size() == 1);
    }

    @Test
    public void EventStoreWriterIsNotCalledWhenCommandResultsInNoEvents() throws Exception {

        // Arrange
        //
        String expectedStreamName = "streamprefix-" + aggregateId.toString();
        List<Event> events = new ArrayList<>();
        events.add(new SimpleEvent(aggregateId));

        EventStoreReader eventStoreReader = mock(EventStoreReader.class);
        when(eventStoreReader.loadEventStream(expectedStreamName)).thenReturn(new ListEventStream(0, events));

        EventStoreWriter eventStoreWriter = mock(EventStoreWriter.class);

        CommandHandler commandHandler = new CommandHandler("streamprefix",
                eventStoreReader::loadEventStream,
                eventStoreWriter::store,
                SomeAggregate.class);

        // Act
        //
        commandHandler.handle(new AnotherCommand(aggregateId));

        // Assert
        //
        verify(eventStoreWriter, never()).store(eq(expectedStreamName), anyLong(), anyList());
    }

    @Test
    public void CommandHandlerLoadsExistingEventsForCorrectStreamWhenStreamNamePassedInAsMethodParameter() throws Exception {

        // Arrange
        //
        String expectedStreamName = "newname";
        List<Event> events = new ArrayList<>();
        events.add(new SimpleEvent(aggregateId));

        EventStoreReader eventStoreReader = mock(EventStoreReader.class);
        when(eventStoreReader.loadEventStream(expectedStreamName)).thenReturn(new ListEventStream(0, events));

        EventStoreWriter eventStoreWriter = mock(EventStoreWriter.class);

        CommandHandler commandHandler = new CommandHandler("streamprefix",
                eventStoreReader::loadEventStream,
                eventStoreWriter::store,
                SomeAggregate.class);

        // Act
        //
        commandHandler.handle(new SimpleCommand(aggregateId), "newname");

        // Assert
        //
        verify(eventStoreReader).loadEventStream(expectedStreamName);
    }

    @Test
    public void EventStoreWriterStoresNewEventsToStreamPassedInToHandleMethod() throws Exception {

        // Arrange
        //
        List<Event> events = new ArrayList<>();
        events.add(new SimpleEvent(aggregateId));

        EventStoreReader eventStoreReader = mock(EventStoreReader.class);
        when(eventStoreReader.loadEventStream("streamname")).thenReturn(new ListEventStream(0, events));

        EventStoreWriter eventStoreWriter = mock(EventStoreWriter.class);
        ArgumentCaptor<List> capturedEvents = ArgumentCaptor.forClass(List.class);

        CommandHandler commandHandler = new CommandHandler("streamname",
                eventStoreReader::loadEventStream,
                eventStoreWriter::store,
                SomeAggregate.class);

        // Act
        //
        commandHandler.handle(new SimpleCommand(aggregateId), "streamname");

        // Assert
        //
        verify(eventStoreWriter).store(eq("streamname"), anyLong(), capturedEvents.capture());

        // check that the one event raised by the aggregate was passed to the writer
        assertThat("Expected 1 event", capturedEvents.getAllValues().size() == 1);
    }

    @Test
    public void CommandHandlerCallsCommandHandlerLookupForTheGivenCommand() throws Exception {

        // Arrange
        //
        Command command = new SimpleCommand(aggregateId);

        CommandHandlerLookup commandHandlerLookup = mock(CommandHandlerLookup.class);
        when(commandHandlerLookup.newAggregateInstance(command)).thenReturn(new SomeAggregate(aggregateId));

        EventStoreReader eventStoreReader = mock(EventStoreReader.class);
        when(eventStoreReader.loadEventStream("streamprefix-" + aggregateId.toString())).thenReturn(new ListEventStream(0, new ArrayList<>()));

        EventStoreWriter eventStoreWriter = mock(EventStoreWriter.class);

        CommandHandler commandHandler = new CommandHandler("streamprefix",
                eventStoreReader::loadEventStream,
                eventStoreWriter::store,
                commandHandlerLookup);

        // Act
        //
        commandHandler.handle(command);

        // Assert
        //
        verify(commandHandlerLookup).newAggregateInstance(command);
    }

    @Test
    public void DeprecatedConstructorDoesNotBreakStuff() throws Exception {

        // Arrange
        //
        Command command = new SimpleCommand(aggregateId);

        EventStoreReader eventStoreReader = mock(EventStoreReader.class);
        when(eventStoreReader.loadEventStream("streamprefix-" + aggregateId.toString())).thenReturn(new ListEventStream(0, new ArrayList<>()));

        EventStoreWriter eventStoreWriter = mock(EventStoreWriter.class);
        ArgumentCaptor<List> capturedEvents = ArgumentCaptor.forClass(List.class);

        CommandHandler commandHandler = new CommandHandler(eventStoreReader,
                eventStoreWriter,
                "streamprefix",
                new ObjectMapper(),
                SomeAggregate.class);

        // Act
        //
        commandHandler.handle(command);

        // Assert
        //
        verify(eventStoreWriter).store(eq("streamprefix-" + aggregateId.toString()), anyLong(), capturedEvents.capture());

        // check that the one event raised by the aggregate was passed to the writer
        assertThat("Expected 1 event", capturedEvents.getAllValues().size() == 1);
    }
}