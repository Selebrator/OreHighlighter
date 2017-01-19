package de.selebrator.orehighlighter.reflection;

import java.lang.reflect.Field;

public interface IFieldAccessor {

	Field getField();

	Object get(Object instance);

	void set(Object instance, Object value);
}
