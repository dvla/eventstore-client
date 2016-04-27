package gov.dvla.osl.eventsourcing.store.http.reader;

import gov.dvla.osl.eventsourcing.store.http.entity.Entry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InOrder;
import rx.Subscriber;

import static org.mockito.Mockito.*;

public class StreamEntryProcessorTest {

    @Test
    public void If_An_Entry_Is_Null_Then_It_Should_Not_Be_Sent_To_Subscriber() {

        // Arrange
        //
        StreamEntryProcessor streamEntryProcessor = new StreamEntryProcessor();

        List<Entry> entries = new ArrayList<>();
        entries.add(null);

        Entry validEntry = constructValidEntry(1);
        entries.add(validEntry);

        Subscriber subscriber = mock(Subscriber.class);

        // Act
        //
        streamEntryProcessor.provideEntriesToSubscriber(entries, subscriber);

        // Assert
        //
        verify(subscriber, never()).onNext(null);
        verify(subscriber, times(1)).onNext(validEntry);
    }


    @Test
    public void If_An_Entry_Has_A_Null_EventNumber_Then_It_Should_Not_Be_Sent_To_Subscriber() {

        // Arrange
        //
        StreamEntryProcessor streamEntryProcessor = new StreamEntryProcessor();

        List<Entry> entries = new ArrayList<>();

        Entry entryOne = new Entry();
        entryOne.setEventNumber(null);
        entries.add(entryOne);

        Entry entryTwo = new Entry();
        entryTwo.setEventNumber(null);
        entries.add(entryTwo);

        Entry validEntry = constructValidEntry(1);
        entries.add(validEntry);

        Subscriber subscriber = mock(Subscriber.class);

        // Act
        //
        streamEntryProcessor.provideEntriesToSubscriber(entries, subscriber);

        // Assert
        //
        verify(subscriber, never()).onNext(entryOne);
        verify(subscriber, never()).onNext(entryTwo);
        verify(subscriber, times(1)).onNext(validEntry);

    }

    @Test
    public void If_An_Entry_Has_A_Null_EventType_Then_It_Should_Not_Be_Sent_To_Subscriber() {

        // Arrange
        //
        StreamEntryProcessor streamEntryProcessor = new StreamEntryProcessor();

        List<Entry> entries = new ArrayList<>();

        Entry entryOne = new Entry();
        entryOne.setEventType(null);
        entries.add(entryOne);

        Entry entryTwo = new Entry();
        entryTwo.setEventType(null);
        entries.add(entryTwo);

        Entry validEntry = constructValidEntry(1);
        entries.add(validEntry);

        Subscriber subscriber = mock(Subscriber.class);

        // Act
        //
        streamEntryProcessor.provideEntriesToSubscriber(entries, subscriber);

        // Assert
        //
        verify(subscriber, never()).onNext(entryOne);
        verify(subscriber, never()).onNext(entryTwo);
        verify(subscriber, times(1)).onNext(validEntry);

    }

    @Test
    public void If_An_Entry_Has_Been_Hard_Deleted_Then_It_Should_Not_Be_Sent_To_Subscriber() {

        // Arrange
        //
        StreamEntryProcessor streamEntryProcessor = new StreamEntryProcessor();

        List<Entry> entries = new ArrayList<>();

        Entry entry = new Entry();
        entry.setEventType("$metadata");
        entries.add(entry);

        Entry validEntry = constructValidEntry(1);
        entries.add(validEntry);

        Subscriber subscriber = mock(Subscriber.class);

        // Act
        //
        streamEntryProcessor.provideEntriesToSubscriber(entries, subscriber);

        // Assert
        //
        verify(subscriber, never()).onNext(entry);
        verify(subscriber, times(1)).onNext(validEntry);

    }

    @Test
    public void If_An_Entry_Is_Valid_It_Should_Be_Sent_To_Subscriber() {

        // Arrange
        //
        StreamEntryProcessor streamEntryProcessor = new StreamEntryProcessor();

        List<Entry> entries = new ArrayList<>();

        Entry validEntry = constructValidEntry(1);
        entries.add(validEntry);

        Subscriber subscriber = mock(Subscriber.class);

        // Act
        //
        streamEntryProcessor.provideEntriesToSubscriber(entries, subscriber);

        // Assert
        //
        verify(subscriber, times(1)).onNext(validEntry);
    }

    @Test
    public void Only_Valid_Entries_Should_Be_Sent_To_Subscriber() {

        // Arrange
        //
        StreamEntryProcessor streamEntryProcessor = new StreamEntryProcessor();

        List<Entry> entries = new ArrayList<>();

        Entry entryOne = constructValidEntry(0);
        entries.add(entryOne);
        Entry entryTwo = constructValidEntry(1);
        entries.add(entryTwo);

        Entry invalidEntry = new Entry();
        invalidEntry.setEventNumber(10);
        invalidEntry.setEventType(null);
        entries.add(invalidEntry);

        Subscriber subscriber = mock(Subscriber.class);

        // Act
        //
        streamEntryProcessor.provideEntriesToSubscriber(entries, subscriber);

        // Assert
        //
        verify(subscriber, times(1)).onNext(entryOne);
        verify(subscriber, times(1)).onNext(entryTwo);
        verify(subscriber, times(0)).onNext(invalidEntry);
    }

    /**
     The event store returns the list of entries in a page in reverse order.  This test checks that we
     present those entries in the correct order to the subscriber.
     */
    @Test
    public void Entries_Should_Be_Sent_To_Subscriber_In_Reverse_Order() {

        // Arrange
        //
        StreamEntryProcessor streamEntryProcessor = new StreamEntryProcessor();

        List<Entry> entries = new ArrayList<>();

        Entry entryOne = constructValidEntry(0);
        Entry entryTwo = constructValidEntry(1);
        Entry entryThree = constructValidEntry(2);

        // Add the entries in reverse order
        entries.add(entryThree);
        entries.add(entryTwo);
        entries.add(entryOne);

        Subscriber subscriber = mock(Subscriber.class);

        InOrder inOrder = inOrder(subscriber);

        // Act
        //
        streamEntryProcessor.provideEntriesToSubscriber(entries, subscriber);

        // Assert
        //
        inOrder.verify(subscriber).onNext(entryOne);
        inOrder.verify(subscriber).onNext(entryTwo);
        inOrder.verify(subscriber).onNext(entryThree);
    }

    private Entry constructValidEntry(int eventNumber) {
        Entry entry = new Entry();
        entry.setEventType("sometype");
        entry.setEventNumber(eventNumber);
        return entry;
    }
}
