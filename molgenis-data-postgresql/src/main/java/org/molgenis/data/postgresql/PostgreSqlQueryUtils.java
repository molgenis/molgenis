package org.molgenis.data.postgresql;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.molgenis.data.meta.AttributeType.ONE_TO_MANY;
import static org.molgenis.data.support.EntityTypeUtils.isMultipleReferenceType;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

/**
 * Utility class used by other PostgreSQL classes
 */
class PostgreSqlQueryUtils
{
	private PostgreSqlQueryUtils()
	{
	}

	/**
	 * Returns attributes persisted by PostgreSQL (e.g. no compound attributes and attributes with an expression)
	 *
	 * @return stream of persisted attributes
	 */
	static Stream<Attribute> getPersistedAttributes(EntityType entityType)
	{
		return StreamSupport.stream(entityType.getAtomicAttributes().spliterator(), false)
							.filter(atomicAttr -> atomicAttr.getExpression() == null);
	}

	/**
	 * Returns all non-bidirectional attributes persisted by PostgreSQL in junction tables (e.g. no compound attributes and attributes
	 * with an expression)
	 *
	 * @return stream of attributes persisted by PostgreSQL in junction tables
	 */
	static Stream<Attribute> getJunctionTableAttributes(EntityType entityType)
	{
		// return all attributes referencing multiple entities except for one-to-many attributes that are mapped by
		// another attribute
		return getPersistedAttributes(entityType).filter(
				attr -> isMultipleReferenceType(attr) && !(attr.getDataType() == ONE_TO_MANY && attr.isMappedBy()));
	}

	/**
	 * Returns all attributes persisted by PostgreSQL in entity table (e.g. no compound attributes and attributes
	 * with an expression)
	 *
	 * @return stream of persisted non-MREF attributes
	 */
	static Stream<Attribute> getTableAttributes(EntityType entityType)
	{
		return getPersistedAttributes(entityType).filter(PostgreSqlQueryUtils::isTableAttribute);
	}

	static boolean isTableAttribute(Attribute attr)
	{
		return !isMultipleReferenceType(attr) && !(attr.getDataType() == ONE_TO_MANY && attr.isMappedBy());
	}

	static Stream<Attribute> getTableAttributesReadonly(EntityType entityType)
	{
		return getTableAttributes(entityType).filter(Attribute::isReadOnly);
	}

	/**
	 * Returns whether the given entity is persisted in PostgreSQL
	 *
	 * @param entityType entity meta data
	 * @return true is the entity is persisted in PostgreSQL
	 */
	static boolean isPersistedInPostgreSql(EntityType entityType)
	{
		String backend = entityType.getBackend();
		if (backend == null)
		{
			// TODO remove this check after getBackend always returns the backend
			if (null != getApplicationContext())
			{
				DataService dataService = getApplicationContext().getBean(DataService.class);
				backend = dataService.getMeta().getDefaultBackend().getName();
			}
			else
			{
				// A workaround for the integration tests. getApplicationContext() should not be null
				return true;
			}
		}
		return backend.equals(PostgreSqlRepositoryCollection.POSTGRESQL);
	}
}
