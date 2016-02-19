package gov.dvla.osl.eventsourcing.projection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gov.dvla.osl.eventsourcing.store.httpeventstore.EventStoreStream;

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
     * @param eventProcessor implementation of EventProcessor
     */
    public ProjectionProcessor(final String streamUrl, final EventProcessor eventProcessor) {
        this.streamUrl = streamUrl;
        this.eventProcessor = eventProcessor;
    }

    public void projectEvents() throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        int nextVersionNumber = eventProcessor.projectionVersionService().getNextVersionNumber();

        String stream = this.streamUrl + "/" + nextVersionNumber + "/forward/20";

        EventStoreStream categoryStream = new EventStoreStream(stream, true);

        categoryStream.readStreamEventsForward().subscribe(
                (event) -> {
                    try {
                        eventProcessor.processEvent(event);
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                },
                (error) -> {
                    LOGGER.error(error.getMessage(), error);
                },
                () -> LOGGER.debug("Dealer projection finished")
        );
    }
}
