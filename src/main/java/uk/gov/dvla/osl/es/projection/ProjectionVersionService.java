package uk.gov.dvla.osl.es.projection;

public interface ProjectionVersionService {
    int getNextVersionNumber();
    void saveProjectionVersion(int version);
}
