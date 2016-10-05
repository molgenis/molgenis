package org.molgenis.data.postgresql;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;

import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.molgenis.data.support.EntityMetaDataUtils.isMultipleReferenceType;
import static org.molgenis.util.ApplicationContextProvider.getApplicationContext;

/**
 * Utility class used by other PostgreSQL classes
 */
class PostgreSqlQueryUtils
{
	static final String JUNCTION_TABLE_ORDER_ATTR_NAME = "order";

	private PostgreSqlQueryUtils()
	{
	}

	/**
	 * Returns the double-quoted table name based on entity name
	 *
	 * @param entityMeta entity meta data
	 * @return table name for this entity
	 */
	static String getTableName(EntityMetaData entityMeta)
	{
		return getTableName(entityMeta, true);
	}

	/**
	 * Returns the table name based on entity name
	 *
	 * @param entityMeta entity meta data
	 * @return PostgreSQL table name
	 */
	static String getTableName(EntityMetaData entityMeta, boolean quoteSystemIdentifiers)
	{
		StringBuilder strBuilder = new StringBuilder(32);
		if (quoteSystemIdentifiers)
		{
			strBuilder.append('"');
		}
		strBuilder.append(entityMeta.getName());
		if (quoteSystemIdentifiers)
		{
			strBuilder.append('"');
		}
		return strBuilder.toString();
	}

	/**
	 * Returns the junction table name for the given attribute of the given entity
	 *
	 * @param entityMeta entity meta data that owns the attribute
	 * @param attr       attribute
	 * @return PostgreSQL junction table name
	 */
	static String getJunctionTableName(EntityMetaData entityMeta, Attribute attr)
	{
		if (attr.isMappedBy())
		{
			return '"' + attr.getRefEntity().getName() + '_' + attr.getMappedBy().getName() + '"';
		}
		else
		{
			return '"' + entityMeta.getName() + '_' + attr.getName() + '"';
		}
	}

	/**
	 * Returns the junction table index name for the given indexed attribute in a junction table
	 *
	 * @param entityMeta entity meta data
	 * @param attr       attribute
	 * @param idxAttr    indexed attribute
	 * @return PostgreSQL junction table index name
	 */
	static String getJunctionTableIndexName(EntityMetaData entityMeta, Attribute attr,
			Attribute idxAttr)
	{
		if (attr.isMappedBy())
		{
			return '"' + attr.getRefEntity().getName() + '_' + attr.getMappedBy().getName() + '_' + idxAttr.getName() + "_idx\"";
		}
		else
		{
			return '"' + entityMeta.getName() + '_' + attr.getName() + '_' + idxAttr.getName() + "_idx\"";
		}
	}

	/**
	 * Returns attributes persisted by PostgreSQL (e.g. no compound attributes and attributes with an expression)
	 *
	 * @return stream of persisted attributes
	 */
	static Stream<Attribute> getPersistedAttributes(EntityMetaData entityMeta)
	{
		return StreamSupport.stream(entityMeta.getAtomicAttributes().spliterator(), false)
				.filter(atomicAttr -> atomicAttr.getExpression() == null);
	}

	/**
	 * Returns all non-bidirectional attributes persisted by PostgreSQL in junction tables (e.g. no compound attributes and attributes
	 * with an expression)
	 *
	 * @return stream of attributes persisted by PostgreSQL in junction tables
	 */
	static Stream<Attribute> getJunctionTableAttributes(EntityMetaData entityMeta)
	{
		// return all attributes referencing multiple entities except for one-to-many attributes that are mapped by
		// another attribute
		return getPersistedAttributes(entityMeta)
				.filter(attr -> isMultipleReferenceType(attr) || (attr.isInversedBy() && isMultipleReferenceType(
						attr.getInversedBy())));
	}

	/**
	 * Returns all attributes persisted by PostgreSQL in entity table (e.g. no compound attributes and attributes
	 * with an expression)
	 *
	 * @return stream of persisted non-MREF attributes
	 */
	static Stream<Attribute> getTableAttributes(EntityMetaData entityMeta)
	{
		return getPersistedAttributes(entityMeta)
				.filter(attr -> !isMultipleReferenceType(attr) && !attr.isInversedBy());
	}

	/**
	 * Returns whether the given entity is persisted in PostgreSQL
	 *
	 * @param entityMeta entity meta data
	 * @return true is the entity is persisted in PostgreSQL
	 */
	static boolean isPersistedInPostgreSql(EntityMetaData entityMeta)
	{
		String backend = entityMeta.getBackend();
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
