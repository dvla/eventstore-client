package gov.dvla.osl.eventsourcing.projection;

import gov.dvla.osl.eventsourcing.api.EventProcessor;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.impl.DefaultEventDeserialiser;
import gov.dvla.osl.eventsourcing.store.http.*;
import gov.dvla.osl.eventsourcing.store.http.reader.HttpEventStoreReader;
import gov.dvla.osl.eventsourcing.store.http.reader.StreamDataFetcher;
import gov.dvla.osl.eventsourcing.store.http.reader.StreamEntryProcessor;
import gov.dvla.osl.eventsourcing.store.http.reader.StreamLinkProcessor;
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

    private EventStoreConfiguration configuration;
    private EventProcessor eventProcessor;
    private HttpEventStoreReader categoryStream;

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

        categoryStream = new HttpEventStoreReader(
                this.configuration,
                new StreamEntryProcessor(),
                new StreamLinkProcessor(),
                new StreamDataFetcher(eventService, this.configuration.getProjectionConfiguration().getLongPollSeconds()),
                new DefaultEventDeserialiser());

        categoryStream.readStreamEventsForward(getNextVersionNumber).retryWhen(errors -> {
            return errors.flatMap(error -> {
                if (error.hasThrowable())
                    LOGGER.error("An error occurred processing the stream", error.getThrowable());
                return Observable.timer(configuration.getProjectionConfiguration().getSecondsBeforeRetry(), TimeUnit.SECONDS);
            });
        }).subscribe(
            (event) -> eventProcessor.processEvent(event),
            (error) -> LOGGER.error(error.getMessage(), error),
            () -> LOGGER.debug("Projection finished")
        );
    }

    public void shutdown() {
        this.categoryStream.shutdown();
    }
}
