package httpeventstore;

import gov.dvla.osl.eventsourcing.store.httpeventstore.StreamLinkProcessor;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.Link;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class StreamLinkProcessorTest {

    @Test
    public void Getting_Link_By_Name_With_No_Match_Must_Return_Empty_String() {

        // Arrange
        //
        List<Link> links = new ArrayList<>();

        links.add(addLink("somerelation", "someuri"));

        StreamLinkProcessor streamLinkProcessor = new StreamLinkProcessor();

        // Act
        //
        String link = streamLinkProcessor.getUriByRelation(links, "previous");

        // Assert
        //
        assertEquals("", link);
    }

    @Test
    public void Getting_Link_By_Name_With_Empty_List_Must_Return_Empty_String() {

        // Arrange
        //
        List<Link> links = new ArrayList<>();

        StreamLinkProcessor streamLinkProcessor = new StreamLinkProcessor();

        // Act
        //
        String link = streamLinkProcessor.getUriByRelation(links, "previous");

        // Assert
        //
        assertEquals("", link);
    }

    @Test
    public void Getting_Link_By_Name_Found_Link_Must_Return_The_Link_Name() {

        // Arrange
        //
        List<Link> links = new ArrayList<>();

        links.add(addLink("previous", "someuri"));

        StreamLinkProcessor streamLinkProcessor = new StreamLinkProcessor();

        // Act
        //
        String link = streamLinkProcessor.getUriByRelation(links, "previous");

        // Assert
        //
        assertEquals("someuri", link);
    }

    private Link addLink(String relation, String uri) {

        Link link = new Link();

        link.setRelation(relation);
        link.setUri(uri);

        return link;
    }
}
