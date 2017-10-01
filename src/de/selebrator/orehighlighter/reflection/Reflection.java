package de.selebrator.orehighlighter.reflection;

import org.apache.commons.lang.ClassUtils;

import java.lang.reflect.AccessibleObject;
import java.util.Arrays;
import java.util.Optional;

public class Reflection {

	/**
	 * handle exception form native method
	 *
	 * @param name fully qualified name of the desired class (not the canonical name)
	 * @return class object representing the desired class
	 */
	public static Class<?> getClass(String name) {
		try {
			return Class.forName(name);
		} catch(ClassNotFoundException e) {
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
		return getField(clazz, Object.class, name, 0);
	}

	public static <T> FieldAccessor<T> getField(Class<?> clazz, Class<T> fieldType, String name) {
		return getField(clazz, fieldType, name, 0);
	}

	public static <T> FieldAccessor<T> getField(Class<?> clazz, Class<T> fieldType, int skip) {
		return getField(clazz, fieldType, null, skip);
	}

	public static <T> FieldAccessor<T> getField(Class<?> clazz, Class<T> fieldType, String name, int skip) {
		Optional<FieldAccessor<T>> fieldAccessor = Arrays.stream(clazz.getDeclaredFields())
				.filter(field -> name == null || field.getName().equals(name))
				.filter(field -> ClassUtils.isAssignable(field.getType(), fieldType, true))
				.skip(skip)
				.map(Reflection::setAccessible)
				.map(field -> (FieldAccessor<T>) () -> field)
				.findFirst();

		if(fieldAccessor.isPresent())
			return fieldAccessor.get();
		else if(clazz.getSuperclass() != null) //search in superclass
			return getField(clazz.getSuperclass(), fieldType, name, 0);
		else
			throw new IllegalArgumentException(String.format("Cannot find field %s %s.", fieldType.getName(), name));
	}

	public static <T> MethodAccessor<T> getMethod(Class<?> clazz, Class<T> returnType, String name, Class<?>... parameterTypes) {
		Optional<MethodAccessor<T>> methodAccessor = Arrays.stream(clazz.getDeclaredMethods())
				.filter(method -> name == null || method.getName().equals(name))
				.filter(method -> returnType == null || method.getReturnType().equals(returnType))
				.filter(method -> Arrays.equals(method.getParameterTypes(), parameterTypes))
				.map(Reflection::setAccessible)
				.map(method -> (MethodAccessor<T>) () -> method)
				.findFirst();

		if(methodAccessor.isPresent())
			return methodAccessor.get();
		else if(clazz.getSuperclass() != null) //search in superclass
			return getMethod(clazz.getSuperclass(), returnType, name, parameterTypes);
		else
			throw new IllegalArgumentException(String.format("Cannot find method %s (%s).", name, Arrays.asList(parameterTypes)));
	}

	public static <T> ConstructorAccessor<T> getConstructor(Class<?> clazz, Class<?>... parameterTypes) {
		Optional<ConstructorAccessor<T>> constructorAccessor = Arrays.stream(clazz.getDeclaredConstructors())
				.filter(constructor -> Arrays.equals(constructor.getParameterTypes(), parameterTypes))
				.map(Reflection::setAccessible)
				.map(constructor -> (ConstructorAccessor<T>) () -> constructor)
				.findFirst();

		if(constructorAccessor.isPresent())
			return constructorAccessor.get();
		else
			throw new IllegalArgumentException(String.format("Cannot find constructor %s (%s).", clazz.getName(), Arrays.asList(parameterTypes)));
	}

	private static <T extends AccessibleObject> T setAccessible(T object) {
		object.setAccessible(true);
		return object;
	}
}
