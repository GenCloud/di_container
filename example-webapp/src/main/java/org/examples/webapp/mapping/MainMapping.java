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
package org.examples.webapp.mapping;

import org.examples.webapp.domain.entity.TblAccount;
import org.examples.webapp.responces.IMessage;
import org.examples.webapp.service.AccountService;
import org.examples.webapp.service.LocaleService;
import org.ioc.annotations.context.IoCDependency;
import org.ioc.annotations.web.IoCController;
import org.ioc.web.annotations.*;
import org.ioc.web.model.ModelAndView;
import org.ioc.web.model.http.RequestEntry;
import org.ioc.web.model.session.HttpSession;
import org.ioc.web.security.configuration.SecurityConfigureAdapter;

import static java.util.concurrent.TimeUnit.MINUTES;

/**
 * @author GenCloud
 * @date 10/2018
 */
@IoCController
@UrlMapping("/")
public class MainMapping {
	@IoCDependency
	private AccountService accountService;

	@IoCDependency
	private LocaleService localeService;

	@IoCDependency
	private SecurityConfigureAdapter sca;

	@UrlMapping
	public ModelAndView index(RequestEntry requestEntry) {
		final ModelAndView modelAndView = new ModelAndView();
		final HttpSession session = sca.getContext().findSession(requestEntry);
		if (session != null) {
			modelAndView.addAttribute("locale", session.getLocale());
			modelAndView.addAttribute("helloMsg", localeService.getMessage("hello", session.getLocale()));
		}

		modelAndView.setView("index");
		return modelAndView;
	}

	@UrlMapping(value = "/signup", method = MappingMethod.POST)
	@RateLimit(timeUnit = MINUTES)
	public IMessage createUser(@RequestParam("username") String username,
							   @RequestParam("password") String password,
							   @RequestParam("repeatedPassword") String repeatedPassword) {
		return accountService.tryCreateUser(username, password, repeatedPassword);
	}

	@UrlMapping(value = "/signin", method = MappingMethod.POST)
	public IMessage auth(RequestEntry requestEntry,
						 @RequestParam("username") String username,
						 @RequestParam("password") String password) {
		return accountService.tryAuthenticateUser(requestEntry, username, password);
	}

	@UrlMapping("/signout")
	public IMessage signout(RequestEntry requestEntry) {
		return accountService.logout(requestEntry);
	}

	@UrlMapping("/loginPage")
	public ModelAndView authenticated(@Credentials TblAccount account) {
		final ModelAndView modelAndView = new ModelAndView();
		modelAndView.setView("auth");

		modelAndView.addAttribute("account", account);
		return modelAndView;
	}
}
