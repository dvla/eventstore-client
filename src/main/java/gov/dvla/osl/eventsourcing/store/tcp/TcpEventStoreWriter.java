package gov.dvla.osl.eventsourcing.store.tcp;

import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import eventstore.EsException;
import eventstore.EventData;
import eventstore.Settings;
import eventstore.WriteEventsCompleted;
import eventstore.j.EsConnection;
import eventstore.j.EsConnectionFactory;
import eventstore.j.EventDataBuilder;
import eventstore.j.WriteEventsBuilder;
import eventstore.tcp.ConnectionActor;
import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.api.EventStoreWriter;
import gov.dvla.osl.eventsourcing.api.Sneak;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfigurationToMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TcpEventStoreWriter implements EventStoreWriter {

    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpEventStoreWriter.class);

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final ActorSystem system;
    private final ActorRef connectionActor;
    private final ActorRef writeResult;
    private final ObjectMapper mapper;
    private final EsConnection connection;
    private final long timeoutSeconds;

    /**
     * Standard constructor
     * @param mapper - object mapper which we will use for encoding events
     * @param config - configuration object for the eventstore client
     */
    public TcpEventStoreWriter(final ObjectMapper mapper,
                                final EventStoreConfiguration config) {
        this.mapper = mapper;
        this.timeoutSeconds = config.getTimeoutSeconds();

        final Map<String, Object> overrides = new EventStoreConfigurationToMap(config).asMap();

        // Get default configuration by applying standard approach
        final Config akkaConfig = ConfigFactory.load();
        // Override with supplied config where appropriate
        final Config combined;
        if (overrides != null) {
            combined = ConfigFactory.parseMap(overrides).withFallback(akkaConfig);
        } else {
            combined = akkaConfig;
        }

        // Retained "default" as name to ensure same basic behaviour as previous version
        this.system = ActorSystem.create("default", combined);

        connectionActor = system.actorOf(ConnectionActor.getProps(Settings.apply(combined)));
        writeResult = system.actorOf(Props.create(WriteResult.class));
        this.connection = EsConnectionFactory.create(system, Settings.apply(combined));
    }

    @Override
    public void store(String streamName, long expectedVersion, List<Event> events) {
        final CompletableFuture<WriteEventsCompleted> future = new CompletableFuture<>();
        final ActorRef writeResult = system.actorOf(Props.create(NotifyingWriteResult.class, future));
        store(streamName, expectedVersion, events, writeResult);

        try {
            future.get(this.timeoutSeconds, TimeUnit.SECONDS);
        } catch (InterruptedException|ExecutionException |TimeoutException exception) {
            LOGGER.error(exception.getMessage(), exception);
            throw new RuntimeException("Failed to store events to event store", exception);
        }
    }

    @Override
    public void store(String streamName, long expectedVersion, Event event) {

    }

    private void store(String streamName, long version, List<Event> events, ActorRef writeResult) {
        WriteEventsBuilder builder = new WriteEventsBuilder(streamName);
        for (Event event : events) {
            builder = builder.addEvent(toEventData(event));
        }
        if (version >= 0) {
            builder = builder.expectVersion((int) version);
        } else {
            builder = builder.expectNoStream();
        }
        connectionActor.tell(builder.build(), writeResult);
    }

    private EventData toEventData(Event event) {
        try {
            return new EventDataBuilder(event.getClass().getName())
                    .eventId(UUID.randomUUID())
                    .jsonData(mapper.writeValueAsString(event))
                    .build();
        } catch (JsonProcessingException exception) {
            throw Sneak.sneakyThrow(exception);
        }
    }

    public static class WriteResult extends UntypedActor {
        final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof WriteEventsCompleted) {
                final WriteEventsCompleted completed = (WriteEventsCompleted) message;
                log.info("range: {}, position: {}", completed.numbersRange(), completed.position());
            } else if (message instanceof Status.Failure) {
                final Status.Failure failure = ((Status.Failure) message);
                final EsException exception = (EsException) failure.cause();
                log.error(exception, exception.toString());
            } else {
                unhandled(message);
            }
        }
    }

    public static class NotifyingWriteResult extends UntypedActor {

        final LoggingAdapter log = Logging.getLogger(getContext().system(), this);

        private final CompletableFuture<WriteEventsCompleted> notifyingFuture;

        public NotifyingWriteResult(CompletableFuture<WriteEventsCompleted> notifyingFuture) {
            this.notifyingFuture = notifyingFuture;
        }

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof WriteEventsCompleted) {
                final WriteEventsCompleted completed = (WriteEventsCompleted) message;
                log.info("range: {}, position: {}", completed.numbersRange(), completed.position());
                this.notifyingFuture.complete((WriteEventsCompleted) message);
            } else if (message instanceof Status.Failure) {
                final Status.Failure failure = ((Status.Failure) message);
                final EsException exception = (EsException) failure.cause();
                log.error(exception, exception.toString());
            } else {
                unhandled(message);
            }
        }
    }
}
