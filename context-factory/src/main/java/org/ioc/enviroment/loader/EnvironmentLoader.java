/*
 * Copyright (c) 2018 IoC Starter (Owner: Maxim Ivanov) authors and/or its affiliates. All rights reserved.
 *
 * This file is part of IoC Starter Project.
 *
 * IoC Starter Project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * IoC Starter Project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with IoC Starter Project.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ioc.enviroment.loader;

import org.ioc.annotations.configuration.Property;
import org.ioc.enviroment.listeners.IEnvironmentFact;
import org.ioc.enviroment.storetypes.IEnvironmentFormatter;
import org.ioc.enviroment.storetypes.impl.EnvironmentFormatterIni;
import org.ioc.enviroment.typecaster.EnvironmentCaster;
import org.ioc.enviroment.typecaster.exception.IllegalEnvironmentException;
import org.ioc.exceptions.IoCException;

import java.io.*;
import java.lang.reflect.*;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Configuration readValues parses given configuration file(s) and fills given object fields.
 * <p>
 *
 * @author GenCloud
 * @date 09/2018
 */
public class EnvironmentLoader {
	private final static Pattern parametersPattern = Pattern.compile("\\$\\{([^}]*)\\}");

	/**
	 * Parses property set with using of annotations.
	 *
	 * @param object annotated object, that represents Java property storage.
	 * @param path   Path to configuration file.
	 * @throws IOException               If configuration file does not exists or due to system IO errors.
	 * @throws IllegalAccessException    If tries access inaccessible entities in annotated object.
	 * @throws InstantiationException    When failed to create instance of an custom object. Such exception can appered when property field is of custom bag.
	 * @throws NoSuchMethodException     Appears on adding splitter properties to lists.
	 * @throws InvocationTargetException Failed invoke some annotated method.
	 */
	@SuppressWarnings({"unchecked", "deprecation"})
	private static Properties parse0(Object object, Properties props, String path) throws Exception {
		final boolean callEvents = object instanceof IEnvironmentFact;

		if (callEvents) {
			((IEnvironmentFact) object).preParseEnvironment(path);
		}

		final boolean isClass = (object instanceof Class);
		boolean classAnnotationPresent;
		String prefix = null;
		boolean classAllowParameters = false;
		if (isClass) {
			classAnnotationPresent = ((Class) object).isAnnotationPresent(Property.class);

			if (classAnnotationPresent) {
				prefix = ((Property) ((Class) object).getAnnotation(Property.class)).prefix();
				classAllowParameters = ((Property) ((Class) object).getAnnotation(Property.class)).parametrize();
			}
		} else {
			classAnnotationPresent = object.getClass().isAnnotationPresent(Property.class);

			if (classAnnotationPresent) {
				prefix = object.getClass().getAnnotation(Property.class).prefix();
				classAllowParameters = object.getClass().getAnnotation(Property.class).parametrize();
			}
		}

		final Field[] fields = (object instanceof Class) ? ((Class) object).getDeclaredFields() : object.getClass().getDeclaredFields();
		for (Field field : fields) {
			String name;
			boolean allowParameters = classAllowParameters;
			if (isClass && !Modifier.isStatic(field.getModifiers())) {
				continue;
			}

			if (field.isAnnotationPresent(Property.class)) {
				if (field.getAnnotation(Property.class).ignore()) {
					continue;
				}

				name = field.getAnnotation(Property.class).value();

				if (!allowParameters) {
					allowParameters = field.getAnnotation(Property.class).parametrize();
				}

				if (name.isEmpty()) {
					name = field.getName();
				}
			} else if (classAnnotationPresent) {
				name = field.getName();
			} else {
				continue;
			}

			if (prefix != null && !prefix.isEmpty()) {
				name = prefix.concat(name);
			}

			field.setAccessible(true);
			if (props.containsKey(name)) {
				String propValue = props.getProperty(name);

				if (allowParameters) {
					boolean replacePlaceholders = false;
					while (true) {
						Matcher parametersMatcher = parametersPattern.matcher(propValue);
						boolean exit = true;
						while (parametersMatcher.find()) {
							final String parameterPropertyName = parametersMatcher.group(1);

							if (!parameterPropertyName.isEmpty()) {
								exit = false;

								String parameterPropertyValue = props.containsKey(parameterPropertyName) ? props.getProperty(parameterPropertyName) : "";
								propValue = propValue.replace(parametersMatcher.group(), parameterPropertyValue);
							} else if (!replacePlaceholders) {
								replacePlaceholders = true;
							}
						}

						if (exit) {
							break;
						}
					}

					if (replacePlaceholders) {
						propValue = propValue.replace("${}", "$");
					}
				}

				if (field.getType().isArray()) {
					final Class baseType = field.getType().getComponentType();
					if (propValue != null) {
						final String[] values = propValue.split(field.isAnnotationPresent(Property.class) ? field.getAnnotation(Property.class).splitter() : ";");

						final Object array = Array.newInstance(baseType, values.length);
						field.set(object, array);

						int index = 0;
						for (String value : values) {
							try {
								Array.set(array, index, EnvironmentCaster.cast(baseType, value));
							} catch (IllegalEnvironmentException | NumberFormatException e) {
								if (callEvents) {
									((IEnvironmentFact) object).typeCastException(name, value);
								}
							}
							++index;
						}

						field.set(object, array);
					}
				} else if (field.getType().isAssignableFrom(List.class)) {
					if (field.get(object) == null) {
						throw new IoCException("Cannot use null-object for parsing List splitter.");
					}

					final Class genericType = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
					if (propValue != null) {
						final String[] values = propValue.split(field.isAnnotationPresent(Property.class) ? field.getAnnotation(Property.class).splitter() : ";");

						for (String value : values) {
							try {
								((List<Object>) field.get(object)).add(EnvironmentCaster.cast(genericType, value));
							} catch (IllegalEnvironmentException | NumberFormatException e) {
								if (callEvents) {
									((IEnvironmentFact) object).typeCastException(name, value);
								}
							}
						}
					}
				} else {
					if (propValue != null) {
						if (EnvironmentCaster.isCast(field)) {
							try {
								EnvironmentCaster.cast(object, field, propValue);
							} catch (IllegalEnvironmentException | NumberFormatException e) {
								if (callEvents) {
									((IEnvironmentFact) object).typeCastException(name, propValue);
								}
							}
						} else {
							final Constructor construct = field.getType().getDeclaredConstructor(String.class);
							construct.setAccessible(true);
							field.set(object, construct.newInstance(propValue));
							construct.setAccessible(false);
						}
					}
				}
			} else {
				if (object instanceof IEnvironmentFact) {
					((IEnvironmentFact) object).missPropertyEvent(name);
				}
			}
			field.setAccessible(false);
		}

		final Method[] methods = (object instanceof Class) ? ((Class) object).getDeclaredMethods() : object.getClass().getDeclaredMethods();
		for (Method method : methods) {
			if (method.isAnnotationPresent(Property.class)) {
				String propName = method.getAnnotation(Property.class).value();

				if (propName.isEmpty()) {
					propName = method.getName();
				}

				if (!props.containsKey(propName)) {
					if (object instanceof IEnvironmentFact) {
						((IEnvironmentFact) object).missPropertyEvent(propName);
					}
					continue;
				}

				if (method.getParameterTypes().length == 1) {
					final String propValue = props.getProperty(propName);
					method.setAccessible(true);
					if (propValue != null) {
						try {
							method.invoke(object, EnvironmentCaster.cast(method.getParameterTypes()[0], propValue));
						} catch (IllegalEnvironmentException | NumberFormatException | InvocationTargetException e) {
							if (callEvents) {
								((IEnvironmentFact) object).typeCastException(propName, propValue);
							}
						}
					} else {
						method.invoke(object, method.getParameterTypes()[0] == String.class ? propValue : (EnvironmentCaster.cast(method.getParameterTypes()[0], propValue)));
					}

					method.setAccessible(false);
				}
			}
		}

		if (callEvents) {
			((IEnvironmentFact) object).postParseEnvironment(path);
		}

		return props;
	}

	/**
	 * Stores configuration fields to stream.
	 *
	 * @param object Object to get fields from.
	 * @throws IllegalAccessException When failed to access objects' visitors.
	 */
	private static void store(Object object, IEnvironmentFormatter formatter) throws IllegalAccessException {
		final boolean isClass = (object instanceof Class);
		final boolean classAnnotationPresent;
		String prefix = null;
		if (isClass) {
			classAnnotationPresent = ((Class) object).isAnnotationPresent(Property.class);

			if (classAnnotationPresent) {
				prefix = ((Property) ((Class) object).getAnnotation(Property.class)).prefix();
			}
		} else {
			classAnnotationPresent = object.getClass().isAnnotationPresent(Property.class);

			if (classAnnotationPresent) {
				prefix = object.getClass().getAnnotation(Property.class).prefix();
			}
		}

		final Field[] fields = (object instanceof Class) ? ((Class) object).getDeclaredFields() : object.getClass().getDeclaredFields();
		for (Field field : fields) {
			// Find property name
			String name, value;
			String splitter = ";";
			if (isClass && !Modifier.isStatic(field.getModifiers())) {
				continue;
			}

			if (field.isAnnotationPresent(Property.class)) {
				if (field.getAnnotation(Property.class).ignore()) {
					continue;
				}

				name = field.getAnnotation(Property.class).value();
				splitter = field.getAnnotation(Property.class).splitter();

				if (name.isEmpty()) {
					name = field.getName();
				}
			} else if (classAnnotationPresent) {
				name = field.getName();
			} else {
				continue;
			}

			if (prefix != null && !prefix.isEmpty()) {
				name = prefix.concat(name);
			}

			field.setAccessible(true);
			final Object fieldValue = field.get(object);

			if (fieldValue != null && field.getType().isArray()) {
				final StringBuilder builder = new StringBuilder();
				for (int i = 0, j = Array.getLength(fieldValue); i < j; ++i) {
					builder.append(Array.get(fieldValue, i));
					if (i < j - 1) {
						builder.append(splitter);
					}
				}
				value = builder.toString();
			} else if (fieldValue != null && field.getType().isAssignableFrom(List.class)) {
				final StringBuilder builder = new StringBuilder();
				boolean isFirst = true;
				for (Object val : (List<?>) fieldValue) {
					if (isFirst) {
						isFirst = false;
					} else {
						builder.append(splitter);
					}

					builder.append(val);
				}
				value = builder.toString();
			} else {
				value = String.valueOf(fieldValue);
			}

			formatter.addPair(name, value);
			field.setAccessible(false);
		}
	}

	/**
	 * Parses property set with using of annotations and string path to property source.
	 *
	 * @param object annotated object, that represents Java property storage.
	 * @param path   Path to properties file.
	 * @throws InvocationTargetException Failed invoke some annotated method.
	 * @throws NoSuchMethodException     Appears on adding splitter properties to lists.
	 * @throws InstantiationException    When failed to create instance of an custom object. Such exception can appered when property field is of custom bag.
	 * @throws IllegalAccessException    If tries access inaccessible entities in annotated object.
	 * @throws IOException               If configuration file does not exists or due to system IO errors.
	 */
	public static Properties parse(Object object, String path) throws Exception {
		return parse(object, new File(path));
	}

	/**
	 * Parses property set with using of annotations and File object referenced to property source.
	 *
	 * @param object annotated object, that represents Java property storage.
	 * @param file   File to read properties from.
	 * @throws InvocationTargetException Failed invoke some annotated method.
	 * @throws NoSuchMethodException     Appears on adding splitter properties to lists.
	 * @throws InstantiationException    When failed to create instance of an custom object. Such exception can appered when property field is of custom bag.
	 * @throws IllegalAccessException    If tries access inaccessible entities in annotated object.
	 * @throws IOException               If configuration file does not exists or due to system IO errors.
	 */
	public static Properties parse(Object object, File file) throws Exception {
		Properties props;

		try (FileInputStream stream = new FileInputStream(file)) {
			props = parse(object, stream, file.getPath());
		}

		return props;
	}

	/**
	 * Parses property set with using of annotations and using abstract input io stream (it can be a file, network or any other thing Java can provide within io streams).
	 *
	 * @param object     annotated object, that represents Java property storage.
	 * @param stream     IO stream from properties will be read.
	 * @param streamName Name of stream (this will be used instead of file name, because of using IO stream we cannot retrieve file name).
	 * @throws InvocationTargetException Failed invoke some annotated method.
	 * @throws NoSuchMethodException     Appears on adding splitter properties to lists.
	 * @throws InstantiationException    When failed to create instance of an custom object. Such exception can appered when property field is of custom bag.
	 * @throws IllegalAccessException    If tries access inaccessible entities in annotated object.
	 * @throws IOException               If configuration file does not exists or due to system IO errors.
	 */
	public static Properties parse(Object object, InputStream stream, String streamName) throws Exception {
		final Properties props = new Properties();
		props.load(stream);

		return parse0(object, props, streamName);
	}

	/**
	 * Parses property set with using of annotations and using abstract input io stream reader (it can be a file, network or any other thing Java can provide within io streams).
	 *
	 * @param object     annotated object, that represents Java property storage.
	 * @param reader     IO stream reader.
	 * @param streamName Name of stream (this will be used instead of file name, because of using IO stream we cannot retrieve file name).
	 * @throws InvocationTargetException Failed invoke some annotated method.
	 * @throws NoSuchMethodException     Appears on adding splitter properties to lists.
	 * @throws InstantiationException    When failed to create instance of an custom object. Such exception can appered when property field is of custom bag.
	 * @throws IllegalAccessException    If tries access inaccessible entities in annotated object.
	 * @throws IOException               If configuration file does not exists or due to system IO errors.
	 */
	public static Properties parse(Object object, Reader reader, String streamName) throws Exception {
		final Properties props = new Properties();
		props.load(reader);

		return parse0(object, props, streamName);
	}

	/**
	 * Stores configuration fields to file with given path.
	 *
	 * @param object Object to get fields from.
	 * @param path   Path to file write to.
	 * @throws IOException            If any I/O error occurs.
	 * @throws IllegalAccessException When failed to access objects' visitors.
	 */
	public static void store(Object object, String path) throws IOException, IllegalAccessException {
		store(object, new File(path));
	}

	/**
	 * Stores configuration fields to given file.
	 *
	 * @param object Object to get fields from.
	 * @param file   File descriptor to write to.
	 * @throws IOException            If any I/O error occurs.
	 * @throws IllegalAccessException When failed to access objects' visitors.
	 */
	public static void store(Object object, File file) throws IOException, IllegalAccessException {
		try (OutputStream stream = new FileOutputStream(file)) {
			store(object, stream);
		}
	}

	/**
	 * Stores configuration fields to stream.
	 *
	 * @param object Object to get fields from.
	 * @param stream Output stream to write to.
	 * @throws IOException            If any I/O error occurs.
	 * @throws IllegalAccessException When failed to access objects' visitors.
	 */
	public static void store(Object object, OutputStream stream) throws IOException, IllegalAccessException {
		final EnvironmentFormatterIni formatter = new EnvironmentFormatterIni();
		store(object, formatter);
		stream.write(formatter.generate().getBytes());
	}

	/**
	 * Stores configuration fields to stream.
	 *
	 * @param object Object to get fields from.
	 * @param writer Writer interface to write to.
	 * @throws IOException            If any I/O error occurs.
	 * @throws IllegalAccessException When failed to access objects' visitors.
	 */
	public static void store(Object object, Writer writer) throws IOException, IllegalAccessException {
		final EnvironmentFormatterIni formatter = new EnvironmentFormatterIni();
		store(object, formatter);
		writer.write(formatter.generate());
	}
}
