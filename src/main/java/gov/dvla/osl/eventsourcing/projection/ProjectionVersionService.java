package gov.dvla.osl.eventsourcing.projection;

public interface ProjectionVersionService {
    int getNextVersionNumber();
    void saveProjectionVersion(int version);
}
