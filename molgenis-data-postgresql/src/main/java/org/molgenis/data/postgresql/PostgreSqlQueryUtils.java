package org.molgenis.data.postgresql;

import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.EntityMetaDataUtils;

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
	public static String getTableName(EntityMetaData entityMeta)
	{
		return getTableName(entityMeta, true);
	}

	/**
	 * Returns the table name based on entity name
	 *
	 * @param emd
	 * @return
	 */
	public static String getTableName(EntityMetaData emd, boolean quoteSystemIdentifiers)
	{
		StringBuilder strBuilder = new StringBuilder();
		if (quoteSystemIdentifiers)
		{
			strBuilder.append("\"");
		}
		strBuilder.append(emd.getName());
		if (quoteSystemIdentifiers)
		{
			strBuilder.append("\"");
		}
		return strBuilder.toString();
	}

	/**
	 * Returns the function table name for the given attribute of the given entity
	 *
	 * @param emd
	 * @param attr
	 * @return
	 */
	public static String getJunctionTableName(EntityMetaData emd, AttributeMetaData attr)
	{
		return new StringBuilder().append("\"").append(emd.getName()).append('_').append(attr.getName()).append("\"")
				.toString();
	}

	/**
	 * Returns attributes persisted by PostgreSQL (e.g. no compound attributes and attributes with an expression)
	 *
	 * @return
	 */
	public static Stream<AttributeMetaData> getPersistedAttributes(EntityMetaData entityMeta)
	{
		return StreamSupport.stream(entityMeta.getAtomicAttributes().spliterator(), false)
				.filter(atomicAttr -> atomicAttr.getExpression() == null);
	}

	/**
	 * Returns all MREF attributes persisted by PostgreSQL (e.g. no compound attributes and attributes with an
	 * expression)
	 *
	 * @return
	 */
	public static Stream<AttributeMetaData> getPersistedAttributesMref(EntityMetaData entityMeta)
	{
		return getPersistedAttributes(entityMeta).filter(EntityMetaDataUtils::isMultipleReferenceType);
	}

	/**
	 * Returns all non-MREF attributes persisted by PostgreSQL (e.g. no compound attributes and attributes with an
	 * expression)
	 *
	 * @return
	 */
	public static Stream<AttributeMetaData> getPersistedAttributesNonMref(EntityMetaData entityMeta)
	{
		return getPersistedAttributes(entityMeta).filter(attr -> !isMultipleReferenceType(attr));
	}

	/**
	 * Returns whether the given entity is persisted in PostgreSQL
	 *
	 * @param entityMeta entity meta data
	 * @return true is the entity is persisted in PostgreSQL
	 */
	public static boolean isPersistedInPostgreSql(EntityMetaData entityMeta)
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
