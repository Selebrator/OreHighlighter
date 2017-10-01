package de.selebrator.orehighlighter.reflection;

import java.lang.reflect.Field;

@FunctionalInterface
public interface FieldAccessor<T> {

	Field getField();

	@SuppressWarnings("unchecked")
	default T get(Object instance) {
		try {
			return (T) this.getField().get(instance);
		} catch(IllegalAccessException e) {
			throw new RuntimeException("Cannot access Reflection.", e);
		}
	}

	default void set(Object instance, T value) {
		try {
			this.getField().set(instance, value);
		} catch(IllegalAccessException e) {
			throw new RuntimeException("Cannot access Reflection.", e);
		}
	}
}
