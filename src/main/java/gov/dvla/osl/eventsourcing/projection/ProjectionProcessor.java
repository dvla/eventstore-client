package gov.dvla.osl.eventsourcing.projection;

import gov.dvla.osl.eventsourcing.api.EventProcessor;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.store.httpeventstore.EventStoreService;
import gov.dvla.osl.eventsourcing.store.httpeventstore.ServiceGenerator;
import gov.dvla.osl.eventsourcing.store.httpeventstore.entity.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import gov.dvla.osl.eventsourcing.store.httpeventstore.EventStoreStream;
import rx.Observable;
import rx.functions.Func0;

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
    private EventStoreStream categoryStream;

    /**
     * Constructor.
     *
     * @param configuration  the configuration information
     * @param eventProcessor implementation of EventProcessor
     */
    public ProjectionProcessor(final EventStoreConfiguration configuration,
                               final EventProcessor eventProcessor) {
        this.configuration = configuration;
        this.eventProcessor = eventProcessor;
    }

    /**
     * Subscribes to all events in a stream from a saved starting point.
     * @throws IOException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void projectEvents(Func0<Integer> getNextVersionNumber) throws IOException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        EventStoreService eventService = ServiceGenerator.createService(EventStoreService.class, this.configuration);

        categoryStream = new EventStoreStream(eventService, this.configuration);

        categoryStream.readStreamEventsForward(getNextVersionNumber).retryWhen(errors -> {
            return errors.flatMap(error -> {
                if (error.hasThrowable())
                    LOGGER.error("An error occurred processing the stream", error.getThrowable());
                return Observable.timer(configuration.getProjectionConfiguration().getSecondsBeforeRetry(), TimeUnit.SECONDS);
            });
        }).subscribe(
                (event) -> {
//                    try {
                        eventProcessor.processEvent(event);
//                    } catch (Exception e) {
//                        LOGGER.error(e.getMessage(), e);
//                        throw e;
//                    }
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
