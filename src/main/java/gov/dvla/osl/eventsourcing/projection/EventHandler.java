package gov.dvla.osl.eventsourcing.projection;

import gov.dvla.osl.eventsourcing.api.Event;
import gov.dvla.osl.eventsourcing.exception.EventHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class EventHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventHandler.class);

    private final Map<Class, MethodHandle> methodHandles = new HashMap<>();

    protected EventHandler() {
        final Method[] declaredMethods = getGenericTypeClass().getDeclaredMethods();

        final List<Method> methods = Arrays.asList(declaredMethods);
        methods.stream().filter(m -> m.getName().equals("handle"))
                .filter(m -> m.getParameterTypes()[0] != Event.class)
                .map(m -> new HandlerMethodMapping(m.getParameterTypes()[0], getMethodHandle(m)))
                .forEach(mapping -> methodHandles.put(mapping.clazz, mapping.methodHandle));
    }

    public void handle(final Event event) throws EventHandlerException {
        if (methodHandles.containsKey(event.getClass())) {
            final MethodHandle methodHandle = methodHandles.get(event.getClass());
            try {
                methodHandle.invoke(this, event);
            } catch (Throwable throwable) {
                throw new EventHandlerException(event.getClass().getCanonicalName(), throwable);
            }
        } else {
            LOGGER.info("Handler not found for " + event.getClass().getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    private Class<T> getGenericTypeClass() {
        try {
            final String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
            final Class<?> clazz = Class.forName(className);
            return (Class<T>) clazz;
        } catch (Exception e) {
            throw new IllegalStateException("Class is not parametrized with generic type!!! Please use extends <> ");
        }
    }

    private MethodHandle getMethodHandle(final Method method) {
        try {
            return MethodHandles.lookup().findVirtual(
                    getGenericTypeClass(),
                    method.getName(),
                    MethodType.methodType(void.class, method.getParameterTypes()[0]));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private class HandlerMethodMapping<T extends Event> {

        private final Class<T> clazz;
        private final MethodHandle methodHandle;

        private HandlerMethodMapping(final Class<T> clazz, final MethodHandle method) {
            this.clazz = clazz;
            this.methodHandle = method;
        }
    }
}
