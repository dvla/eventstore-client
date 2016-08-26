package uk.gov.dvla.osl.eventsourcing.impl;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.dvla.osl.eventsourcing.api.Command;

public class CommandHandlerLookup {

	private static final Logger LOGGER = LoggerFactory.getLogger(CommandHandlerLookup.class);

	private Map<Class<? extends Command>, Class<?>> commandHandlers = new HashMap<>();

	@SuppressWarnings("unchecked")
	public CommandHandlerLookup(String methodName, Class<?>... aggregateTypes) {
		for (Class<?> type : aggregateTypes) {
			for (Method method : type.getMethods()) {
				if (method.getName().equals(methodName) &&
						method.getParameterTypes().length == 1 &&
						Command.class.isAssignableFrom(method.getParameterTypes()[0])) {
					commandHandlers.put((Class<? extends Command>) method.getParameterTypes()[0], type);
				}
			}
		}
	}

	public Class<?> targetType(Command command) {
		return commandHandlers.get(command.getClass());
	}

	public Object newAggregateInstance(final Command command) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		final Class<?> clazz = targetType(command);
		final Constructor<?> ctor = clazz.getConstructor(UUID.class);
		LOGGER.debug("Creating aggregate: " + clazz);
		return ctor.newInstance(command.aggregateId());
	}
}