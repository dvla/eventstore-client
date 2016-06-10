package uk.gov.dvla.osl.eventsourcing.api;

import uk.gov.dvla.osl.eventsourcing.store.http.entity.Link;

import java.util.List;

public interface LinkProcessor {
    String getUriByRelation(final List<Link> links, final String relationName);
}
