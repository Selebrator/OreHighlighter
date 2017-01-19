package de.selebrator.orehighlighter.reflection;

public interface MethodAccessor<T> {

	T invoke(Object instance, Object... args);
}
