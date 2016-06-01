package gov.dvla.osl.eventsourcing.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.api.EventDeserialiser;
import gov.dvla.osl.eventsourcing.exception.EventDeserialisationException;
import org.junit.Assert;
import org.junit.Test;
import gov.dvla.osl.eventsourcing.store.memory.SomeEvent;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

public class DefaultEventDeserialiserTest {

    @Test(expected = EventDeserialisationException.class)
    public void invalidDataMustThrowEventDeserialisationException() throws IOException, ClassNotFoundException {

        // Arrange
        //
        String data = "yadayada";
        EventDeserialiser eventDeserialiser = new DefaultEventDeserialiser();

        // Act
        //
        eventDeserialiser.deserialise(data, "gov.dvla.osl.eventsourcing.store.memory.SomeEvent");
    }

    @Test(expected = EventDeserialisationException.class)
    public void invalidEventTypeMustThrowEventDeserialisationException() throws IOException, ClassNotFoundException {

        // Arrange
        //
        String data = "yadayada";
        EventDeserialiser eventDeserialiser = new DefaultEventDeserialiser();

        // Act
        //
        eventDeserialiser.deserialise(data, "uk.gov.dvla.osl.memory.UnknownEvent");
    }

    @Test
    public void validDataAndEventTypeMustResultInValidEvent() throws IOException, ClassNotFoundException {

        // Arrange
        //
        Date eventDate = new Date();
        UUID eventId = UUID.randomUUID();
        SomeEvent testEvent = new SomeEvent(eventId, "billy", "brag", "emailaddress", eventDate);

        ObjectMapper mapper = new ObjectMapper();
        String data = mapper.writeValueAsString(testEvent);

        // Act
        //
        EventDeserialiser eventDeserialiser = new DefaultEventDeserialiser();
        Event event = eventDeserialiser.deserialise(data, "gov.dvla.osl.eventsourcing.store.memory.SomeEvent");
        SomeEvent someEvent = (SomeEvent)event;

        // Assert
        //
        Assert.assertEquals(eventId, someEvent.driverId);
        Assert.assertEquals("billy", someEvent.forename);
        Assert.assertEquals("brag", someEvent.surname);
        Assert.assertEquals("emailaddress", someEvent.email);
        Assert.assertEquals(eventDate.toString(), someEvent.eventDate.toString());
    }
}
