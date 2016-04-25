package gov.dvla.osl.eventsourcing.impl;


import gov.dvla.osl.eventsourcing.api.Command;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CommandHandlerLookup {

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
}