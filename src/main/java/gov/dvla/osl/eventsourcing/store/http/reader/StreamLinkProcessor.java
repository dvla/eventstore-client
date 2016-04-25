package gov.dvla.osl.eventsourcing.store.http.reader;

import gov.dvla.osl.eventsourcing.api.LinkProcessor;
import gov.dvla.osl.eventsourcing.store.http.entity.Link;

import java.util.List;

public class StreamLinkProcessor implements LinkProcessor {

    @Override
    public String getUriByRelation(List<Link> links, String relationName) {
        for (Link link : links) {
            if (link.getRelation().equals(relationName))
                return link.getUri();
        }
        return "";
    }
}
