package de.selebrator.reflection;

import java.lang.reflect.Constructor;

public interface IConstructorAccessor {

	Constructor<?> getConstructor();

	Object newInstance(Object... parameters);
}
