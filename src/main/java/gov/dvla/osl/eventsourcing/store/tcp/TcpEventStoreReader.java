package gov.dvla.osl.eventsourcing.store.tcp;


import akka.actor.ActorSystem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import eventstore.*;
import eventstore.j.EsConnection;
import eventstore.j.EsConnectionFactory;
import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.api.*;
import gov.dvla.osl.eventsourcing.api.EventStream;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfiguration;
import gov.dvla.osl.eventsourcing.configuration.EventStoreConfigurationToMap;
import gov.dvla.osl.eventsourcing.impl.DefaultEventDeserialiser;
import gov.dvla.osl.eventsourcing.store.http.entity.Entry;
import gov.dvla.osl.eventsourcing.store.memory.ListEventStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;
import rx.functions.Func0;
import scala.concurrent.Future;
import scala.concurrent.duration.Duration;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static rx.Observable.create;

/**
 * EventStoreReader implementation pointing to an actual instance of eventstore, via the eventstore
 * JVM client.
 */
@Deprecated
public class TcpEventStoreReader implements EventStoreReader<Long> {

    /**
     * Logger for this class.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TcpEventStoreReader.class);

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final ActorSystem system;
    private final String streamPrefix;
    private final ObjectMapper mapper;
    private final EsConnection connection;

    /**
     * Standard constructor
     * @param streamPrefix - prefix for the stream to read/write
     * @param mapper - object mapper which we will use for encoding events
     * @param config - configuration object for the eventstore client
     */
    public TcpEventStoreReader(final String streamPrefix, final ObjectMapper mapper,
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

        this.connection = EsConnectionFactory.create(system, Settings.apply(combined));
    }

    @Override
    public EventStream loadEventStream(String aggregateId) {
        final Future<ReadStreamEventsCompleted> future = connection.readStreamEventsForward(streamPrefix + "-" + aggregateId, null, 1000, false, null);
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

    @Override
    public Observable<Entry> readStreamEventsForward(Func0<Integer> getNextVersionNumber) {
        return null;
    }

    @Override
    public void shutdown() {

    }

}