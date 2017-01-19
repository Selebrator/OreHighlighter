package de.selebrator.orehighlighter.reflection;

public interface ConstructorAccessor<T> {

	T newInstance(Object... parameters);
}
