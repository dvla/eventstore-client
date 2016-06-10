package uk.gov.dvla.osl.eventsourcing.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import uk.gov.dvla.osl.eventsourcing.api.Event;
import uk.gov.dvla.osl.eventsourcing.api.EventDeserialiser;
import uk.gov.dvla.osl.eventsourcing.exception.EventDeserialisationException;

public class DefaultEventDeserialiser implements EventDeserialiser {

    private ObjectMapper mapper;

    public DefaultEventDeserialiser() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    }

    @Override
    public Event deserialise(final String data, final String eventType)  {

        Class clazz;
        try {
            clazz = Class.forName(eventType);
            return (Event) mapper.readValue(data, clazz);
        } catch (Exception e) {
            throw new EventDeserialisationException(data, eventType, e);
        }
    }
}
