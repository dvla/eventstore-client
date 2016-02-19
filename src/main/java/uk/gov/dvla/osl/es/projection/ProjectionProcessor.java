package uk.gov.dvla.osl.es.projection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dvla.osl.es.store.httpeventstore.EventStoreStream;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Subscribes to all events in a stream from a starting point determined by the eventProcessor.
 */
public class ProjectionProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectionProcessor.class);

    private final String streamUrl;
    private EventProcessor eventProcessor;

    /**
     * Constructor.
     * @param streamUrl the streamUrl url eg. http://hostname:port/$ce-dealer
     * @param eventProcessor implementaton of EventProcessor
     */
    public ProjectionProcessor(final String streamUrl, final EventProcessor eventProcessor) {
        this.streamUrl = streamUrl;
        this.eventProcessor = eventProcessor;
    }

    public void projectVehicleEvents() throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        int nextVersionNumber = eventProcessor.projectionVersionService().getNextVersionNumber();

        String stream = this.streamUrl + "/" + nextVersionNumber + "/forward/20";

        EventStoreStream categoryStream = new EventStoreStream(stream, true);

        categoryStream.readStreamEventsForward().subscribe(
                (event) -> {
                    try {
                        eventProcessor.processEvent(event);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                (error) -> {
                    LOGGER.debug("Something went wrong");
                    error.printStackTrace();
                },
                () -> LOGGER.debug("Dealer projection finished")
        );
    }
}
