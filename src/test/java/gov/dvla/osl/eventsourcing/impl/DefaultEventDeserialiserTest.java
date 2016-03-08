package gov.dvla.osl.eventsourcing.impl;

import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.api.EventDeserialiser;
import gov.dvla.osl.eventsourcing.exception.EventDeserialisationException;
import org.junit.Assert;
import org.junit.Test;
import uk.gov.dvla.osl.memory.SomeEvent;

import java.io.IOException;
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
        eventDeserialiser.deserialise(data, "uk.gov.dvla.osl.memory.SomeEvent");
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
        String data = "{\n  \"driverId\": \"fbf4a1a1-b4a3-4dfe-a01f-ec52c34e16e4\", \"forename\": \"billy\", \"surname\": \"brag\", \"email\": \"emailaddress\" \n}";
        EventDeserialiser eventDeserialiser = new DefaultEventDeserialiser();

        // Act
        //
        Event event = eventDeserialiser.deserialise(data, "uk.gov.dvla.osl.memory.SomeEvent");
        SomeEvent someEvent = (SomeEvent)event;

        // Assert
        //
        Assert.assertEquals(UUID.fromString("fbf4a1a1-b4a3-4dfe-a01f-ec52c34e16e4"), someEvent.driverId);
        Assert.assertEquals("billy", someEvent.forename);
        Assert.assertEquals("brag", someEvent.surname);
        Assert.assertEquals("emailaddress", someEvent.email);
    }
}
