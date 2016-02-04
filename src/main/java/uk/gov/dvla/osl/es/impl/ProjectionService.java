package uk.gov.dvla.osl.es.impl;

import uk.gov.dvla.osl.es.api.Command;
import uk.gov.dvla.osl.es.api.Event;
import uk.gov.dvla.osl.es.store.memory.EventStore;
import uk.gov.dvla.osl.es.store.memory.EventStream;

import java.util.List;

public class ProjectionService {
    private final EventStore eventStore;
    private CommandHandlerLookup commandHandlerLookup;

    public ProjectionService(EventStore eventStore, Class<?>... aggregateTypes) {
        this.eventStore = eventStore;
        this.commandHandlerLookup = new CommandHandlerLookup(ReflectionUtil.HANDLE_METHOD, aggregateTypes);
    }

    public void handle(Command command) throws Exception {
        EventStream<Long> eventStream = eventStore.loadEventStream(command.aggregateId());
        Object target = newAggregateInstance(command);
        for (Event event : eventStream) {
            ReflectionUtil.invokeHandleMethod(target, event);
        }
        List<Event> events = ReflectionUtil.invokeHandleMethod(target, command);
        if (events != null && events.size() > 0) {
            eventStore.store(command.aggregateId(), eventStream.version(), events);
        } else {
            // Command generated no events
        }
    }

    private Object newAggregateInstance(Command command) throws InstantiationException, IllegalAccessException {
        return commandHandlerLookup.targetType(command).newInstance();
    }
}
