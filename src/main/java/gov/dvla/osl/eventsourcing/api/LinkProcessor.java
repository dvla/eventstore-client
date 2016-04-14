package gov.dvla.osl.eventsourcing.api;

import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.Link;

import java.util.List;

public interface LinkProcessor {
    String getUriByRelation(List<Link> links, String name);
}