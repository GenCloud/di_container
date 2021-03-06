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
package org.ioc.orm.factory.facility;

import org.ioc.orm.exceptions.OrmException;
import org.ioc.orm.factory.DatabaseSessionFactory;
import org.ioc.orm.factory.SchemaQuery;
import org.ioc.orm.metadata.EntityMetadataSelector;
import org.ioc.orm.metadata.transaction.AbstractTx;
import org.ioc.orm.metadata.transaction.Tx;
import org.ioc.orm.metadata.type.FacilityMetadata;
import org.ioc.utils.Assertion;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author GenCloud
 * @date 10/2018
 */
public class FacilityManager extends AbstractTx {
	private final DatabaseSessionFactory databaseSession;
	private final EntityMetadataSelector entityMetadataSelector;

	public FacilityManager(DatabaseSessionFactory databaseSession, EntityMetadataSelector entityMetadataSelector) {
		Assertion.checkNotNull(databaseSession);
		Assertion.checkNotNull(entityMetadataSelector);

		this.databaseSession = databaseSession;
		this.entityMetadataSelector = entityMetadataSelector;
	}

	@Override
	public void close() {
		databaseSession.close();
	}

	public void clear() {
		databaseSession.clear();
	}

	public DatabaseSessionFactory getSession() {
		return databaseSession;
	}

	public <T> SchemaQuery<T> query(Class<T> clazz, String nameOrQuery) throws OrmException {
		return query(clazz, nameOrQuery, Collections.emptyMap());
	}

	@SuppressWarnings("unchecked")
	public <T> SchemaQuery<T> query(Class<T> clazz, String name, Map<String, Object> parameters) throws OrmException {
		Assertion.checkNotNull(clazz, "type");

		final FacilityMetadata metadata = entityMetadataSelector.getMetadata(clazz);
		if (metadata == null) {
			return null;
		}

		return databaseSession.query(metadata, name, parameters);
	}

	public <T> boolean exists(Class<T> clazz, Object key) throws OrmException {
		Assertion.checkNotNull(clazz, "type");

		if (key == null) {
			return false;
		}

		final FacilityMetadata metadata = entityMetadataSelector.getMetadata(clazz);
		if (metadata == null) {
			return false;
		}

		return databaseSession.exists(metadata, key);
	}

	@SuppressWarnings("unchecked")
	public <T> T fetch(Class<? extends T> clazz, Object key) throws OrmException {
		Assertion.checkNotNull(clazz, "type");

		if (key == null) {
			return null;
		}

		final FacilityMetadata metadata = entityMetadataSelector.getMetadata(clazz);
		if (metadata == null) {
			return null;
		}

		final Object o = databaseSession.fetch(metadata, key);
		if (o != null) {
			metadata.invokePostLoad(o);
			return (T) o;
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> List<T> fetchAll(Class<? extends T> clazz) {
		Assertion.checkNotNull(clazz, "type");

		final FacilityMetadata metadata = entityMetadataSelector.getMetadata(clazz);
		if (metadata == null) {
			return null;
		}

		final List<Object> result = databaseSession.fetchAll(metadata);
		if (result != null) {
			result.forEach(metadata::invokePostLoad);
			return (List<T>) result;
		}

		return null;
	}

	public <T> void delete(T element) throws OrmException {
		databaseSession.delete(findMetadata(element), element);
	}

	public <T> void save(T element) throws OrmException {
		databaseSession.save(findMetadata(element), element);
	}

	private <T> FacilityMetadata findMetadata(T element) {
		Assertion.checkNotNull(element, "vertex");

		final Class<?> clazz = element.getClass();
		final FacilityMetadata metadata = entityMetadataSelector.getMetadata(clazz);
		if (metadata == null) {
			throw new OrmException("Could not get metadata for element [" + element + "]. Register it.");
		}

		return metadata;
	}

	@Override
	public Tx openTx() throws OrmException {
		return databaseSession.openTx();
	}

	@Override
	public boolean pending() {
		return databaseSession.pending();
	}

	@Override
	public void start() throws OrmException {
		databaseSession.start();
	}

	@Override
	public void commit() throws OrmException {
		databaseSession.commit();
	}

	@Override
	public void rollback() throws OrmException {
		databaseSession.rollback();
	}
}
