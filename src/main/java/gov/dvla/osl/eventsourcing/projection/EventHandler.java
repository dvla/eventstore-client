package gov.dvla.osl.eventsourcing.projection;

import gov.dvla.osl.eventsourcing.api.Event;
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

    public void handle(final Event event) {
        if (methodHandles.containsKey(event.getClass())) {
            try {
                methodHandles.get(event.getClass()).invoke(this, event);
            } catch (Throwable throwable) {
                LOGGER.error(throwable.getMessage(), throwable);
            }
        } else {
            LOGGER.info("Handler not found for " + event.getClass().getCanonicalName());
        }
    }

    @SuppressWarnings("unchecked")
    private Class<T> getGenericTypeClass() {
        try {
            String className = ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName();
            Class<?> clazz = Class.forName(className);
            return (Class<T>) clazz;
        } catch (Exception e) {
            throw new IllegalStateException("Class is not parametrized with generic type!!! Please use extends <> ");
        }
    }

    private final MethodHandle getMethodHandle(final Method method) {
        try {
            return  MethodHandles.lookup().findVirtual(
                    getGenericTypeClass(),
                    method.getName(),
                    MethodType.methodType(void.class, method.getParameterTypes()[0]));
        } catch (NoSuchMethodException|IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private class HandlerMethodMapping<T extends Event> {

        private final Class<T> clazz;
        private final MethodHandle methodHandle;

        private HandlerMethodMapping(final Class<T> c, final MethodHandle method) {
            this.clazz = c;
            this.methodHandle = method;
        }
    }
}
