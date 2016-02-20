package gov.dvla.osl.eventsourcing.api;

import java.io.IOException;

public interface EventDeserialiser {
    Event deserialise(String data, String eventType) throws ClassNotFoundException, IOException;
}
