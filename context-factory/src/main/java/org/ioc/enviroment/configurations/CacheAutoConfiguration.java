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
package org.ioc.enviroment.configurations;

import org.ioc.annotations.configuration.Property;
import org.ioc.annotations.configuration.PropertyFunction;
import org.ioc.context.factories.Factory;
import org.ioc.utils.ReflectionUtils;

import static org.ioc.context.factories.Factory.defaultCacheFactory;

/**
 * @author GenCloud
 * @date 09/2018
 */
@Property(prefix = "cache.")
public class CacheAutoConfiguration {
	@Property("factory")
	private String factoryClass = "org.ioc.context.factories.cache.EhFactory";

	@PropertyFunction
	@SuppressWarnings("unchecked")
	public Object cacheFactory() {
		final Class<? extends Factory> factory = factoryClass == null ? defaultCacheFactory()
				: (Class<? extends Factory>) ReflectionUtils.loadClass(factoryClass);
		return ReflectionUtils.instantiateClass(factory);
	}
}
