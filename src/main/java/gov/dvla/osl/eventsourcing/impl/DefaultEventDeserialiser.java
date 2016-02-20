package gov.dvla.osl.eventsourcing.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.api.EventDeserialiser;

import java.io.IOException;

public class DefaultEventDeserialiser implements EventDeserialiser {

    private ObjectMapper mapper;

    public DefaultEventDeserialiser() {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
    }

    @Override
    public Event deserialise(String data, String eventType) throws ClassNotFoundException, IOException {
        Class clazz = Class.forName(eventType);
        return (Event) mapper.readValue(data, clazz);
    }
}
