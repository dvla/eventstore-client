package uk.gov.dvla.osl.memory;

import uk.gov.dvla.osl.es.api.Event;

import java.util.UUID;

public class SomeEvent implements Event {
    public final UUID driverId;
    public final String forename;
    public final String surname;
    public final String email;

    SomeEvent() {
        this.driverId = null;
        this.forename = null;
        this.surname = null;
        this.email = null;
    }

    public SomeEvent(UUID driverId, String forename, String surname, String email) {
        this.driverId = driverId;
        this.forename = forename;
        this.surname = surname;
        this.email = email;
    }
}