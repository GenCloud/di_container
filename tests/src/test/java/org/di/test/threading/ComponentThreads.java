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
package org.di.test.threading;

import org.di.context.annotations.IoCComponent;
import org.di.context.annotations.IoCDependency;
import org.di.threads.factory.ThreadingComponent;
import org.di.threads.factory.model.AbstractTask;

import javax.annotation.PostConstruct;

/**
 * @author GenCloud
 * @date 14.09.2018
 */
@IoCComponent
public class ComponentThreads {
    @IoCDependency
    private ThreadingComponent threadingComponent;

    @PostConstruct
    public void init() {
        threadingComponent.async(new AbstractTask<Void>() {
            @Override
            public Void call() {
                System.out.println("Start test thread!");
                return null;
            }
        });
    }
}
