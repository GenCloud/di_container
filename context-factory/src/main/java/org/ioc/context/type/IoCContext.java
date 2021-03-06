package org.ioc.context.type;

import org.ioc.annotations.context.Mode;
import org.ioc.context.factories.core.PrototypeFactory;
import org.ioc.context.factories.core.SingletonFactory;
import org.ioc.context.factories.web.RequestFactory;
import org.ioc.context.model.TypeMetadata;

import java.util.Collection;
import java.util.List;

/**
 * @author GenCloud
 * @date 09/2018
 */
public interface IoCContext {
	String[] getPackages();

	List<TypeMetadata> getTypes();

	/**
	 * Returns the component from the factories.
	 * Depending on its bag, the initialized component or an existing one.
	 *
	 * @param name bag name for find
	 * @return instantiated object from context factories
	 */
	<O> O getType(String name);

	/**
	 * Returns the component from the factories.
	 * Depending on its bag, the initialized component or an existing one.
	 *
	 * @param type bag for find
	 * @return instantiated object from context factories
	 */
	<O> O getType(Class<O> type);

	/**
	 * Custom function of adding instance to factory.
	 *
	 * @param instance instantiated object
	 */
	void setType(String name, Object instance);

	Collection<TypeMetadata> getMetadatas(Mode mode);

	/**
	 * Function of objects instantiation in factories.
	 *
	 * @param types types to init instance
	 * @return instantiated bag's
	 */
	List<TypeMetadata> registerTypes(List<TypeMetadata> types);

	/**
	 * Function of lazy - objects instantiation in factories.
	 *
	 * @param types types to init instance
	 * @return instantiated bag's
	 */
	List<TypeMetadata> registerLazy(List<TypeMetadata> types);

	/**
	 * Calling function of destroying components, if any.
	 */
	void destroy();

	RequestFactory getRequestFactory();

	SingletonFactory getSingletonFactory();

	PrototypeFactory getPrototypeFactory();
}
