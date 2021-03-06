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
package org.ioc.context.model.cache;

import com.google.common.cache.LoadingCache;

import java.util.Iterator;

/**
 * @author GenCloud
 * @date 09/2018
 */
public class GuavaFacade<K, V> implements ICache<K, V> {
	private final com.google.common.cache.LoadingCache<K, V> cache;

	public GuavaFacade(com.google.common.cache.LoadingCache<K, V> cache) {
		this.cache = cache;
	}

	public LoadingCache<K, V> getCache() {
		return cache;
	}

	@Override
	public V put(K key, V value) {
		cache.put(key, value);
		return value;
	}

	@Override
	public V get(K key) {
		return cache.getIfPresent(key);
	}

	@Override
	public boolean contains(K key) {
		return cache.getIfPresent(key) != null;
	}

	@Override
	public V remove(K key) {
		V element = get(key);
		if (element != null) {
			cache.invalidate(key);
		}
		return element;
	}

	@Override
	public void clear() {
		cache.invalidateAll();
	}

	@Override
	public int size() {
		return Math.toIntExact(cache.size());
	}

	@Override
	public Iterator<K> keys() {
		return cache.asMap().keySet().iterator();
	}

	@Override
	public Iterator<V> values() {
		return cache.asMap().values().iterator();
	}
}