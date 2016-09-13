package uk.gov.dvla.osl.eventsourcing.projection;

import uk.gov.dvla.osl.eventsourcing.api.Event;
import uk.gov.dvla.osl.eventsourcing.api.EventDeserialiser;
import uk.gov.dvla.osl.eventsourcing.api.EventProcessor;
import uk.gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import uk.gov.dvla.osl.eventsourcing.impl.DefaultEventDeserialiser;
import uk.gov.dvla.osl.eventsourcing.store.http.reader.HttpEventStoreReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func0;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

/**
 *
 * This class coordinates all the pieces required to do a projection.  It takes a passed in EventProcessor and
 * invokes a method on that to process a single event at a time for all the events it retrieves from the
 * event store.
 */
public class ProjectionProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectionProcessor.class);

    private final EventStoreConfiguration configuration;
    private final EventProcessor eventProcessor;
    private EventDeserialiser eventDeserialiser;
    private HttpEventStoreReader categoryStream;

    /**
     * Constructor.
     *
     * @param configuration  the configuration information
     * @param eventProcessor implementation of EventProcessor
     */
    public ProjectionProcessor(final EventStoreConfiguration configuration,
                               final EventProcessor eventProcessor) {
        this(configuration, eventProcessor, new DefaultEventDeserialiser());
    }

    /**
     * Constructor.
     *
     * @param configuration  the configuration information
     * @param eventProcessor implementation of EventProcessor
     */
    public ProjectionProcessor(final EventStoreConfiguration configuration,
                               final EventProcessor eventProcessor,
                               final EventDeserialiser eventDeserialiser) {
        this.configuration = configuration;
        this.eventProcessor = eventProcessor;
        this.eventDeserialiser = eventDeserialiser;
    }

    /**
     * Subscribes to all events in a stream from a saved starting point.
     * @throws IOException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void projectEvents(Func0<Integer> getNextVersionNumber) throws IOException,
            InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        categoryStream = new HttpEventStoreReader(configuration);

        categoryStream.readStreamEventsForward(
                configuration.getProjectionConfiguration().getStream(),
                getNextVersionNumber.call(),
                configuration.getProjectionConfiguration().getPageSize(),
                configuration.getProjectionConfiguration().isKeepAlive()).retryWhen(errors -> {
            return errors.flatMap(error -> {
                LOGGER.error("An error occurred processing the stream {}", error.getMessage());
                return Observable.timer(configuration.getProjectionConfiguration().getSecondsBeforeRetry(), TimeUnit.SECONDS);
            });
        }).subscribe(
            (event) -> eventProcessor.processEvent(event),
            (error) -> LOGGER.error(error.getMessage(), error),
            () -> LOGGER.debug("Projection finished")
        );
    }

    /**
     * Subscribes to all events in a stream from a saved starting point.
     * @throws IOException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public void projectEvent(Func0<Integer> getNextVersionNumber) throws IOException,
            InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        categoryStream = new HttpEventStoreReader(configuration);

        categoryStream.readStreamEventsForward(
                configuration.getProjectionConfiguration().getStream(),
                getNextVersionNumber.call(),
                configuration.getProjectionConfiguration().getPageSize(),
                configuration.getProjectionConfiguration().isKeepAlive()).retryWhen(errors -> {
            return errors.flatMap(error -> {
                LOGGER.error("An error occurred processing the stream {}", error.getMessage());
                return Observable.timer(configuration.getProjectionConfiguration().getSecondsBeforeRetry(), TimeUnit.SECONDS);
            });
        }).subscribe(
                (entry) -> {
                    Event event = eventDeserialiser.deserialise(entry.getData(), entry.getEventType());
                    eventProcessor.processEvent(new ProjectedEvent(event, entry.getPositionEventNumber()));
                },
                (error) -> LOGGER.error(error.getMessage(), error),
                () -> LOGGER.debug("Projection finished")
        );
    }

    public void shutdown() {
        this.categoryStream.shutdown();
    }
}
