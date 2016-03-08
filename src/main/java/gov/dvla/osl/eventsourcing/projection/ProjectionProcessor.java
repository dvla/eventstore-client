package gov.dvla.osl.eventsourcing.projection;

import gov.dvla.osl.eventsourcing.api.EventProcessor;
import gov.dvla.osl.eventsourcing.api.ProjectionVersionService;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.store.httpeventstore.EventStoreService;
import gov.dvla.osl.eventsourcing.store.httpeventstore.ServiceGenerator;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gov.dvla.osl.eventsourcing.store.httpeventstore.EventStoreStream;
import rx.Observable;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class ProjectionProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectionProcessor.class);

    private EventStoreConfiguration configuration;
    private EventProcessor eventProcessor;
    private ProjectionVersionService projectionVersionService;
    private EventStoreStream categoryStream;

    /**
     * Constructor.
     *
     * @param configuration  the configuration information
     * @param eventProcessor implementation of EventProcessor
     */
    public ProjectionProcessor(final EventStoreConfiguration configuration,
                               final EventProcessor eventProcessor,
                               final ProjectionVersionService projectionVersionService) {
        this.configuration = configuration;
        this.eventProcessor = eventProcessor;
        this.projectionVersionService = projectionVersionService;
    }

    /**
     * Subscribes to all events in a stream from a saved starting point.
     * @throws IOException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void projectEvents() throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        int nextVersionNumber = projectionVersionService.getNextVersionNumber();

        EventStoreService eventService = ServiceGenerator.createService(EventStoreService.class, this.configuration);

        categoryStream = new EventStoreStream(eventService, this.configuration, nextVersionNumber);

        categoryStream.readStreamEventsForward().subscribe(
                (event) -> {
                    try {
                        eventProcessor.processEvent(event);
                        projectionVersionService.saveProjectionVersion(event.getPositionEventNumber());
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

    public void shutdown() {
        this.categoryStream.shutdown();
    }
}
