package de.selebrator.orehighlighter.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

@FunctionalInterface
public interface MethodAccessor<T> {

	Method getMethod();

	@SuppressWarnings("unchecked")
	default T invoke(Object instance, Object... args) {
		try {
			return (T) this.getMethod().invoke(instance, args);
		} catch(IllegalAccessException e) {
			throw new RuntimeException("Cannot access Reflection.", e);
		} catch(InvocationTargetException e) {
			throw new RuntimeException(String.format("Cannot invoke method %s (%s).", this.getMethod().getName(), Arrays.asList(this.getMethod().getParameterTypes())));
		}
	}
}
