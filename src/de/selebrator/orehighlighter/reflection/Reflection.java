package de.selebrator.orehighlighter.reflection;

import org.apache.commons.lang.ClassUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class Reflection {

	/**
	 * handle exception form native method
	 * @param name fully qualified name of the desired class (not the canonical name)
	 * @return class object representing the desired class
	 */
	public static Class<?> getClass(String name) {
		try {
			return Class.forName(name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Class<?> getServerClass(ServerPackage serverPackage, String name) {
		return getClass(serverPackage.toString() + "." + name);
	}

	public static Class<?> getMinecraftClass(String name) {
		return getServerClass(ServerPackage.NMS, name);
	}

	public static Class<?> getCraftBukkitClass(String name) {
		return getServerClass(ServerPackage.OBC, name);
	}

	@SuppressWarnings("unchecked")
	public static Class<? extends Enum> getEnum(Class<?> clazz) {
		if(clazz.isEnum())
			return (Class<? extends Enum>) clazz;

		new IllegalArgumentException(clazz.getCanonicalName() + " is not an enum.").printStackTrace();
		return null;
	}

	public static Class<? extends Enum> getEnum(String name) {
		return getEnum(getClass(name));
	}

	public static Class<? extends Enum> getServerEnum(ServerPackage serverPackage, String name) {
		return getEnum(serverPackage.toString() + "." + name);
	}

	public static Class<? extends Enum> getMinecraftEnum(String name) {
		return getServerEnum(ServerPackage.NMS, name);
	}

	public static Class<? extends Enum> getCraftBukkitEnum(String name) {
		return getServerEnum(ServerPackage.OBC, name);
	}

	public static FieldAccessor getField(Class<?> clazz, String name) {
		return getField(clazz, Object.class, name);
	}

	public static <T> FieldAccessor<T> getField(Class<?> clazz, Class<T> fieldType, String name) {
		return getField(clazz, fieldType, name, 0);
	}

	public static <T> FieldAccessor<T> getField(Class<?> clazz, Class<T> fieldType, int skip) {
		return getField(clazz, fieldType, null, skip);
	}

	public static <T> FieldAccessor<T> getField(Class<?> clazz, Class<T> fieldType, String name, int skip) {
		for(Field field : clazz.getDeclaredFields()) {
			if((name == null || field.getName().equals(name))
					&& ClassUtils.isAssignable(field.getType(), fieldType, true) && skip-- <= 0) {
				field.setAccessible(true);
				return new FieldAccessor<T>() {
					@Override
					@SuppressWarnings("unchecked")
					public T get(Object instance) {
						try {
							return (T) field.get(instance);
						} catch(IllegalAccessException e) {
							throw new RuntimeException("Cannot access Reflection.", e);
						}
					}

					@Override
					public void set(Object instance, T value) {
						try {
							field.set(instance, value);
						} catch(IllegalAccessException e) {
							throw new RuntimeException("Cannot access Reflection.", e);
						}
					}
				};
			}
		}

		//search in superclass
		if(clazz.getSuperclass() != null)
			return getField(clazz.getSuperclass(), fieldType, name);

		throw new IllegalArgumentException(String.format("Cannot find field %s %s.", fieldType.getSimpleName(), name));
	}

	public static <T> MethodAccessor<T> getMethod(Class<?> clazz, Class<T> returnType, String name, Class<?>... parameterTypes) {
		for(Method method : clazz.getDeclaredMethods()) {
			if((name == null || method.getName().equals(name))
					&& (returnType == null || method.getReturnType().equals(returnType))
					&& (Arrays.equals(method.getParameterTypes(), parameterTypes))) {
				method.setAccessible(true);
				return new MethodAccessor<T>() {
					@Override
					@SuppressWarnings("unchecked")
					public T invoke(Object target, Object... args) {
						try {
							return (T) method.invoke(target, args);
						} catch(IllegalAccessException e) {
							throw new RuntimeException("Cannot access Reflection.", e);
						} catch(InvocationTargetException e) {
							throw new RuntimeException(String.format("Cannot invoke method %s (%s).", method.getName(), Arrays.asList(method.getParameterTypes())));
						}
					}
				};
			}
		}

		//search in superclass
		if(clazz.getSuperclass() != null)
			return getMethod(clazz.getSuperclass(), returnType, name, parameterTypes);

		throw new IllegalArgumentException(String.format("Cannot find method %s (%s).", name, Arrays.asList(parameterTypes)));
	}

	public static <T> ConstructorAccessor<T> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
		for(Constructor<?> constructor : clazz.getConstructors()) {
			if(Arrays.equals(constructor.getParameterTypes(), parameterTypes)) {
				constructor.setAccessible(true);
				return  new ConstructorAccessor<T>() {
					@Override
					@SuppressWarnings("unchecked")
					public T newInstance(Object... parameters) {
						try {
							return (T) constructor.newInstance(parameters);
						} catch(InstantiationException e) {
							throw new RuntimeException("Cannot initiate object.", e);
						} catch(IllegalAccessException e) {
							throw new RuntimeException("Cannot access Reflection.", e);
						} catch(InvocationTargetException e) {
							throw new RuntimeException(String.format("Cannot invoke constructor %s (%s).", constructor.getName(), Arrays.asList(constructor.getParameterTypes())));
						}
					}
				};
			}
		}

		throw new IllegalArgumentException(String.format("Cannot find constructor %s (%s).", clazz.getSimpleName(), Arrays.asList(parameterTypes)));
	}
}
