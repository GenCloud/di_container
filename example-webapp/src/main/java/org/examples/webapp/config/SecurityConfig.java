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
package org.examples.webapp.config;

import org.ioc.annotations.configuration.Property;
import org.ioc.annotations.configuration.PropertyFunction;
import org.ioc.context.factories.cache.ExpiringCacheFactory;
import org.ioc.web.interceptors.RateLimitInterceptor;
import org.ioc.web.security.configuration.HttpContainer;
import org.ioc.web.security.configuration.SecurityConfigureProcessor;
import org.ioc.web.security.encoder.Encoder;
import org.ioc.web.security.encoder.bcrypt.BCryptEncoder;
import org.ioc.web.security.filter.CorsFilter;
import org.ioc.web.security.filter.CsrfFilter;

/**
 * @author GenCloud
 * @date 10/2018
 */
@Property
public class SecurityConfig implements SecurityConfigureProcessor {
	@Override
	public void configure(HttpContainer httpContainer) {
		httpContainer.
				configureRequests().
				anonymousRequests("/", "/signup", "/signin").
				resourceRequests("/static/**").
				authorizeRequests("/loginPage", "ROLE_USER").
				authorizeRequests("/signout", "ROLE_USER").
				and().
				configureSession().
				expiredPath("/");
	}

	@PropertyFunction
	public RateLimitInterceptor rateLimitInterceptor() {
		return new RateLimitInterceptor(new ExpiringCacheFactory());
	}

	@PropertyFunction
	public CsrfFilter csrfFilter() {
		return new CsrfFilter();
	}

	@PropertyFunction
	public CorsFilter corsFilter() {
		return new CorsFilter();
	}

	@PropertyFunction
	public Encoder encoder() {
		return new BCryptEncoder();
	}
}
