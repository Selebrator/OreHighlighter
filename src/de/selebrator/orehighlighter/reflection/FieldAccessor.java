package de.selebrator.orehighlighter.reflection;

public interface FieldAccessor<T> {

	T get(Object instance);

	void set(Object instance, T value);
}
