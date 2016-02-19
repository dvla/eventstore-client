package gov.dvla.osl.eventsourcing.impl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ReflectionUtil {
	public static final String HANDLE_METHOD = "handle";
	public static final String SAVE_METHOD = "save";

	@SuppressWarnings("unchecked")
	public static <R> R invokeHandleMethod(Object target, Object param) {
		try {
			Method method = target.getClass().getMethod(ReflectionUtil.HANDLE_METHOD, param.getClass());
			if (method == null) return null;
			return (R) method.invoke(target, param);
		} catch (InvocationTargetException e) {
			throw Sneak.sneakyThrow(e.getTargetException());
		} catch (NoSuchMethodException e) {
			return null;
		} catch (Exception e) {
			throw Sneak.sneakyThrow(e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <R> R invokeSaveMethod(Object target, int version) {
		try {
			Method method = target.getClass().getMethod(ReflectionUtil.SAVE_METHOD, int.class);
			if (method == null) return null;
			return (R) method.invoke(target, version);
		} catch (InvocationTargetException e) {
			throw Sneak.sneakyThrow(e.getTargetException());
		} catch (NoSuchMethodException e) {
			return null;
		} catch (Exception e) {
			throw Sneak.sneakyThrow(e);
		}
	}

	public static <P> P project(P target, Object param) {
		invokeHandleMethod(target, param);
		return target;
	}

}

