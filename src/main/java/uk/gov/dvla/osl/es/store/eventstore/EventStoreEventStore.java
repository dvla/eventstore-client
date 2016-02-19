package uk.gov.dvla.osl.es.store.eventstore;


import akka.actor.*;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import eventstore.*;
import eventstore.j.EsConnection;
import eventstore.j.EsConnectionFactory;
import eventstore.j.EventDataBuilder;
import eventstore.j.WriteEventsBuilder;
import eventstore.tcp.ConnectionActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;
import uk.gov.dvla.osl.es.api.Event;
import uk.gov.dvla.osl.es.api.EventStoreEvent;
import uk.gov.dvla.osl.es.impl.Sneak;
import uk.gov.dvla.osl.es.store.memory.EventStore;
import uk.gov.dvla.osl.es.store.memory.EventStream;
import uk.gov.dvla.osl.es.store.memory.ListEventStream;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static rx.Observable.create;

public class EventStoreEventStore implements EventStore<Long> {
    private static final Logger logger = LoggerFactory.getLogger(EventStoreEventStore.class);

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final ActorSystem system;
    private final String streamPrefix;
    private final ActorRef connectionActor;
    private final ActorRef writeResult;
    private final ObjectMapper mapper;
    private final EsConnection connection;

    public EventStoreEventStore(String streamPrefix, ObjectMapper mapper, final Map overrides) {
        this.streamPrefix = streamPrefix;
        this.mapper = mapper;

        // Get default configuration by applying standard approach
        final Config akkaConfig = ConfigFactory.load();
        // Override with supplied config where appropriate
        final Config combined = overrides != null ? ConfigFactory.parseMap(overrides).withFallback(akkaConfig) : akkaConfig;

        this.system = ActorSystem.create("default", combined); // Retained "default" as name to ensure same basic behaviour as previous version

        connectionActor = system.actorOf(ConnectionActor.getProps(Settings.apply(combined)));
        writeResult = system.actorOf(Props.create(WriteResult.class));
        this.connection = EsConnectionFactory.create(system, Settings.apply(combined));
    }

    @Override
    public EventStream<Long> loadEventStream(UUID aggregateId) {
        final Future<ReadStreamEventsCompleted> future = connection.readStreamEventsForward(streamPrefix + aggregateId, null, 1000, false, null);
        try {
            ReadStreamEventsCompleted result = future.result(Duration.apply(10, TimeUnit.SECONDS), null);
            List<Event> events = new ArrayList<>();
            for (eventstore.Event event : result.eventsJava()) {
                Event domainEvent = parse(event);
                events.add(domainEvent);
            }
            return new ListEventStream(result.lastEventNumber().value(), events);
        } catch (StreamNotFoundException e) {
            return new ListEventStream(-1, Collections.emptyList());
        } catch (Exception e) {
            throw Sneak.sneakyThrow(e);
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
        EventStoreEvent eventStoreEvent = new EventStoreEvent();
        eventStoreEvent.setData(domainEvent.toString());
        eventStoreEvent.setEventNumber(event.number().value());
        return eventStoreEvent;
    }

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
        } catch (InterruptedException|ExecutionException|TimeoutException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to store events to event store", e);
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
        } catch (JsonProcessingException e) {
            throw Sneak.sneakyThrow(e);
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

    @Override
    public Observable<EventStoreEvent> all() {
        return create((Observable.OnSubscribe<EventStoreEvent>) subscriber -> connection.subscribeToAllFrom(new SubscriptionObserver<IndexedEvent>() {

            @Override
            public void onLiveProcessingStart(Closeable arg0) {
                logger.info("live processing started");
            }

            @Override
            public void onEvent(IndexedEvent event, Closeable arg1) {
                if (!event.event().streamId().isSystem() && event.event().streamId().streamId().startsWith("driver")) {
                    try {
                        logger.debug(event.toString());
                        subscriber.onNext(parseEvent(event.event()));
                    } catch (Exception e) {
                        logger.warn("Error when handling event", e);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                system.log().error(e.toString());
                subscriber.onError(e);
            }

            @Override
            public void onClose() {
                logger.error("subscription closed");
                subscriber.onCompleted();
            }
        }, null, false, null));
    }

    @Override
    public Observable<EventStoreEvent> streamFrom(String streamName) {
        return create((Observable.OnSubscribe<EventStoreEvent>) subscriber -> connection.subscribeToStreamFrom(streamName,
                new SubscriptionObserver<eventstore.Event>() {

            @Override
            public void onLiveProcessingStart(Closeable arg0) {
                logger.info("live processing started");
            }

            @Override
            public void onEvent(eventstore.Event event, Closeable arg1) {
                try {
                    subscriber.onNext(parseEvent(event));
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable e) {
                system.log().error(e.toString());
                subscriber.onError(e);
            }

            @Override
            public void onClose() {
                logger.error("subscription closed");
                subscriber.onCompleted();
            }
        }, 0, false, null));
    }

}