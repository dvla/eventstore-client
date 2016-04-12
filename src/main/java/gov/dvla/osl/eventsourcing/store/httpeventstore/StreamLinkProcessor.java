package gov.dvla.osl.eventsourcing.store.httpeventstore;

import gov.dvla.osl.eventsourcing.api.LinkProcessor;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.Link;

import java.util.List;

public class StreamLinkProcessor implements LinkProcessor {

    @Override
    public String getUriByRelation(List<Link> links, String name) {
        for (int i = 0; i < links.size(); i++) {
            if (links.get(i).getRelation().equals(name))
                return links.get(i).getUri();
        }
        return "";
    }
}
