package gov.dvla.osl.eventsourcing.store.eventstore;


import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.Status;
import akka.actor.UntypedActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import eventstore.EsException;
import eventstore.EventData;
import eventstore.IndexedEvent;
import eventstore.ReadStreamEventsCompleted;
import eventstore.Settings;
import eventstore.StreamNotFoundException;
import eventstore.SubscriptionObserver;
import eventstore.WriteEventsCompleted;
import eventstore.j.EsConnection;
import eventstore.j.EsConnectionFactory;
import eventstore.j.EventDataBuilder;
import eventstore.j.WriteEventsBuilder;
import eventstore.tcp.ConnectionActor;
import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.api.EventStoreEvent;
import gov.dvla.osl.eventsourcing.api.Sneak;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfigurationToMap;
import gov.dvla.osl.eventsourcing.impl.DefaultEventDeserialiser;
import gov.dvla.osl.eventsourcing.store.memory.EventStore;
import gov.dvla.osl.eventsourcing.store.memory.ListEventStream;

import rx.Observable;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;


import static rx.Observable.create;

/**
 * Eventstore implementation pointing to an actual instance of eventstore, via the eventstore
 * JVM client.
 */
public class EventStoreEventStore implements EventStore<Long> {

    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(EventStoreEventStore.class);

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final ActorSystem system;
    private final String streamPrefix;
    private final ActorRef connectionActor;
    private final ActorRef writeResult;
    private final ObjectMapper mapper;
    private final EsConnection connection;

    /**
     * Standard constructor
     * @param streamPrefix - prefix for the stream to read/write
     * @param mapper - object mapper which we will use for encoding events
     * @param config - configuration object for the eventstore client
     */
    public EventStoreEventStore(final String streamPrefix, final ObjectMapper mapper,
                                final EventStoreConfiguration config) {
        this.streamPrefix = streamPrefix;
        this.mapper = mapper;

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
    public gov.dvla.osl.eventsourcing.store.memory.EventStream<Long> loadEventStream(UUID aggregateId) {
        final Future<ReadStreamEventsCompleted> future = connection.readStreamEventsForward(streamPrefix + aggregateId, null, 1000, false, null);
        try {
            ReadStreamEventsCompleted result = future.result(Duration.apply(10, TimeUnit.SECONDS), null);
            List<Event> events = new ArrayList<>();
            for (eventstore.Event event : result.eventsJava()) {
                Event domainEvent = parse(event);
                events.add(domainEvent);
            }
            return new ListEventStream(result.lastEventNumber().value(), events);
        } catch (StreamNotFoundException exception) {
            return new ListEventStream(-1, Collections.emptyList());
        } catch (Exception exception) {
            throw Sneak.sneakyThrow(exception);
        }
    }

    private Event parse(eventstore.Event event)
            throws ClassNotFoundException, IOException {
        Class<? extends Event> type = (Class<? extends Event>) Class.forName(event.data().eventType());
        String json = new String(event.data().data().value().toArray(), UTF8);
        Event domainEvent = mapper.readValue(json, type);
        return domainEvent;
    }

    private EventStoreEvent parseEvent(eventstore.Event event)
            throws ClassNotFoundException, IOException {
        Class<? extends Event> type = (Class<? extends Event>) Class.forName(event.data().eventType());
        String json = new String(event.data().data().value().toArray(), UTF8);
        Event domainEvent = mapper.readValue(json, type);
        EventStoreEvent eventStoreEvent = new EventStoreEvent(new DefaultEventDeserialiser());
        eventStoreEvent.setData(domainEvent.toString());
        eventStoreEvent.setEventNumber(event.number().value());
        return eventStoreEvent;
    }

    @Deprecated
    @Override
    public void store(UUID aggregateId, long version, List<Event> events) {
        store(aggregateId, version, events, writeResult);
    }

    @Override
    public void storeBlocking(UUID aggregateId, long version, List<Event> events, long timeout, TimeUnit timeUnit) {
        final CompletableFuture<WriteEventsCompleted> future = new CompletableFuture<>();
        final ActorRef writeResult = system.actorOf(Props.create(NotifyingWriteResult.class, future));
        store(aggregateId, version, events, writeResult);

        try {
            future.get(timeout, timeUnit);
        } catch (InterruptedException|ExecutionException|TimeoutException exception) {
            LOGGER.error(exception.getMessage(), exception);
            throw new RuntimeException("Failed to store events to event store", exception);
        }
    }

    private void store(UUID aggregateId, long version, List<Event> events, ActorRef writeResult) {
        WriteEventsBuilder builder = new WriteEventsBuilder(streamPrefix + aggregateId);
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

    @Deprecated
    @Override
    public Observable<EventStoreEvent> all() {
        return create((Observable.OnSubscribe<EventStoreEvent>) subscriber -> connection.subscribeToAllFrom(new SubscriptionObserver<IndexedEvent>() {

            @Override
            public void onLiveProcessingStart(Closeable arg0) {
                LOGGER.info("live processing started");
            }

            @Override
            public void onEvent(IndexedEvent event, Closeable arg1) {
                if (!event.event().streamId().isSystem() && event.event().streamId().streamId().startsWith("driver")) {
                    try {
                        LOGGER.debug(event.toString());
                        subscriber.onNext(parseEvent(event.event()));
                    } catch (Exception exception) {
                        LOGGER.warn("Error when handling event", exception);
                    }
                }
            }

            @Override
            public void onError(Throwable exception) {
                LOGGER.error(exception.getMessage(), exception);
                subscriber.onError(exception);
            }

            @Override
            public void onClose() {
                LOGGER.error("subscription closed");
                subscriber.onCompleted();
            }
        }, null, false, null));
    }

    @Deprecated
    @Override
    public Observable<EventStoreEvent> streamFrom(String streamName) {
        return create((Observable.OnSubscribe<EventStoreEvent>) subscriber -> connection.subscribeToStreamFrom(streamName,
                new SubscriptionObserver<eventstore.Event>() {

            @Override
            public void onLiveProcessingStart(Closeable arg0) {
                LOGGER.info("live processing started");
            }

            @Override
            public void onEvent(eventstore.Event event, Closeable arg1) {
                try {
                    subscriber.onNext(parseEvent(event));
                } catch (ClassNotFoundException exception) {
                    LOGGER.error(exception.getMessage(), exception);
                } catch (IOException exception) {
                    LOGGER.error(exception.getMessage(), exception);
                }
            }

            @Override
            public void onError(Throwable exception) {
                LOGGER.error(exception.getMessage(), exception);
                subscriber.onError(exception);
            }

            @Override
            public void onClose() {
                LOGGER.error("subscription closed");
                subscriber.onCompleted();
            }
        }, 0, false, null));
    }

}