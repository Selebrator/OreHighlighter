package de.selebrator.reflection;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Reflection {

	public static Class<?> getClass(String path, String name) {
		try {
			return Class.forName(path + "." + name);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Class<?> getClass(ServerPackage path, String name) {
		return getClass(path.toString(), name);
	}

	public static IConstructorAccessor getConstructor(Class<?> clazz, Class<?>... parameterTypes) {

		for(Constructor<?> constructor : clazz.getDeclaredConstructors()) {
			if(isClassListEqual(parameterTypes, constructor.getParameterTypes())){
				constructor.setAccessible(true);
				return new IConstructorAccessor() {

					@Override
					public Object newInstance(Object... parameters) {
						try {
							return constructor.newInstance(parameters);
						} catch (IllegalAccessException e) {
							throw new IllegalStateException("Cannot use reflection.", e);
						} catch (InstantiationException e) {
							throw new RuntimeException("Cannot instantiate object.", e);
						} catch (InvocationTargetException e) {
							throw new RuntimeException("An internal error occured.", e.getCause());
						} catch (IllegalArgumentException e) {
							e.printStackTrace();
							return null;
						}
					}

					@Override
					public Constructor<?> getConstructor() {
						return constructor;
					}
				};
			}
		}

		if(clazz.getSuperclass() != null) {
			return getConstructor(clazz.getSuperclass(), parameterTypes);
		}
		return null;
	}

	public static IMethodAccessor getMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {

		for(Method method : clazz.getDeclaredMethods()) {
			if(method.getName().equals(name)) {
				if(isClassListEqual(parameterTypes, method.getParameterTypes())) {
					method.setAccessible(true);
					return new IMethodAccessor() {

						@Override
						public Object invoke(Object target, Object... args) {
							try {
								return method.invoke(target, args);
							} catch (IllegalAccessException e) {
								throw new IllegalStateException("Cannot use reflection.", e);
							} catch (InvocationTargetException e) {
								return null;
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
								return null;
							}
						}

						@Override
						public Method getMethod() {
							return method;
						}
					};
				}
			}
		}

		if(clazz.getSuperclass() != null) {
			return getMethod(clazz, name, parameterTypes);
		}
		return null;
	}

	public static IFieldAccessor getField(Class<?> clazz, String name) {

		try {
			Field field;
			field = clazz.getDeclaredField(name);
			field.setAccessible(true);
			return new IFieldAccessor() {

				@Override
				public void set(Object instance, Object value) {
					try {
						field.set(instance, value);
					} catch (IllegalAccessException e) {
						throw new IllegalStateException("Cannot use reflection.", e);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					}
				}

				@Override
				public Object get(Object instance) {
					try {
						return field.get(instance);
					} catch (IllegalAccessException e) {
						throw new IllegalStateException("Cannot use reflection.", e);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
						return null;
					}
				}

				@Override
				public Field getField() {
					return field;
				}
			};
		} catch (NoSuchFieldException e) {
			System.out.println("Cannot find field");
			e.printStackTrace();
		} catch (SecurityException e) {
			System.out.println("Couldnot access field");
			e.printStackTrace();
		}

		if(clazz.getSuperclass() != null) {
			return getField(clazz, name);
		}
		return null;
	}

	public static boolean isClassListEqual(Class<?>[] classes1, Class<?>[] classes2) {
		boolean equal = true;
		if (classes1.length != classes2.length) {
			return false;
		}

		for (int i = 0; i < classes1.length; i++) {
			if (classes1[i] != classes2[i]) {
				equal = false;
				break;
			}
		}
		return equal;
	}
}
