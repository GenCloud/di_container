/*
 * Copyright (c) 2018 DI (IoC) Container (Team: GC Dev, Owner: Maxim Ivanov) authors and/or its affiliates. All rights reserved.
 *
 * This file is part of DI (IoC) Container Project.
 *
 * DI (IoC) Container Project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DI (IoC) Container Project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with DI (IoC) Container Project.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.di.context.enviroment.listeners;

/**
 * Properties event listener. Target class should implement this interface to be available to listen nProperty events.
 * In other cases events will not be called.
 *
 * @author GenCloud
 * @date 05.09.2018
 */
public interface IPropertyListener {
    /**
     * When parsing of configuration file starts.
     *
     * @param path Path to configuration file.
     */
    void preParseEnvironment(String path);

    /**
     * When some property is missing.
     *
     * @param name Missed property name.
     */
    void missPropertyEvent(String name);

    /**
     * When done parsing configuration file.
     *
     * @param path File path.
     */
    void postParseEnvironment(String path);

    /**
     * When property value casting is invalid.
     *
     * @param name  Property name.
     * @param value Casted value.
     */
    void typeCastException(String name, String value);
}