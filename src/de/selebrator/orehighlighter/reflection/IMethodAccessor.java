package de.selebrator.orehighlighter.reflection;

import java.lang.reflect.Method;

public interface IMethodAccessor {

	Method getMethod();

	Object invoke(Object target, Object... args);
}
