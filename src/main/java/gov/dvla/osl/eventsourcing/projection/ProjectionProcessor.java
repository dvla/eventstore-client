package gov.dvla.osl.eventsourcing.projection;

import com.fasterxml.jackson.databind.ObjectMapper;
import gov.dvla.osl.eventsourcing.api.EventProcessor;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.store.httpeventstore.EventStoreService;
import gov.dvla.osl.eventsourcing.store.httpeventstore.ServiceGenerator;
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
    private final ObjectMapper mapper;

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
        this.mapper = new ObjectMapper();

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

        categoryStream = new EventStoreStream(eventService, this.configuration, mapper);

        categoryStream.readStreamEventsForward(getNextVersionNumber).retryWhen(errors -> {
            return errors.flatMap(error -> {
                if (error.hasThrowable())
                    LOGGER.error("An error occurred processing the stream", error.getThrowable());
                return Observable.timer(configuration.getProjectionConfiguration().getSecondsBeforeRetry(), TimeUnit.SECONDS);
            });
        }).subscribe(
            (event) -> eventProcessor.processEvent(event),
            (error) -> LOGGER.error(error.getMessage(), error),
            () -> LOGGER.debug("Dealer projection finished")
        );
    }

    public void shutdown() {
        this.categoryStream.shutdown();
    }
}
