package gov.dvla.osl.eventsourcing.api;

public interface ProjectionVersionService {
    int getNextVersionNumber();
    void saveProjectionVersion(int version);
}
