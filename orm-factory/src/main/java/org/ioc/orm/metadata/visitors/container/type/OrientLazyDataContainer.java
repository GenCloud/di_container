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
package org.ioc.orm.metadata.visitors.container.type;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.record.impl.ODocument;
import org.ioc.orm.exceptions.OrmException;
import org.ioc.orm.factory.orient.session.OrientDatabaseSession;
import org.ioc.orm.metadata.relation.mapper.OrientBagMapper;
import org.ioc.orm.metadata.type.ColumnMetadata;
import org.ioc.orm.metadata.type.EntityMetadata;
import org.ioc.orm.metadata.visitors.container.DataContainer;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.ioc.orm.util.OrientUtils.convertValue;

/**
 * @author GenCloud
 * @date 10/2018
 */
public class OrientLazyDataContainer implements DataContainer {
	private final AtomicBoolean loaded = new AtomicBoolean(false);

	private final OrientDatabaseSession databaseSession;
	private final EntityMetadata entityMetadata;
	private final ColumnMetadata columnMetadata;
	private final Object key;

	private ODocument document;

	public OrientLazyDataContainer(OrientDatabaseSession databaseSession, EntityMetadata meta, ColumnMetadata columnMetadata,
								   Object key) {
		this.databaseSession = databaseSession;
		this.entityMetadata = meta;
		this.columnMetadata = columnMetadata;
		this.key = key;
	}

	@Override
	public boolean empty() {
		try {
			return ensureResults().isEmpty();
		} catch (Exception e) {
			throw new OrmException("Unable to query lazy loaded results from [" + entityMetadata + "] columnMetadata [" + columnMetadata + "].", e);
		}
	}

	@Override
	public Object of() throws OrmException {
		final ODocument document = ensureResults();
		if (document == null) {
			return null;
		}
		try {
			return convertValue(document, columnMetadata);
		} catch (Exception e) {
			throw new OrmException("Unable to toEntity lazy loaded results for columnMetadata [" + columnMetadata + "] on entityMetadata [" + entityMetadata + "].", e);
		}
	}

	private ODocument ensureResults() throws OrmException {
		if (loaded.get()) {
			return document;
		}
		try {
			final OIdentifiable record = new OrientBagMapper(entityMetadata).fromKey(databaseSession, key);
			document = databaseSession.findDocument(record);
			loaded.getAndSet(true);
		} catch (Exception e) {
			throw new OrmException("Unable to fetch orientdb document by key [" + key + "].", e);
		}
		return document;
	}
}