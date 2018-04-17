package org.molgenis.data.postgresql;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;

import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.postgresql.PostgreSqlNameGenerator.getColumnName;
import static org.molgenis.data.util.MolgenisDateFormat.parseInstant;
import static org.molgenis.data.util.MolgenisDateFormat.parseLocalDate;

@Component
class PostgreSqlEntityFactory
{
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlEntityFactory.class);

	private final EntityManager entityManager;

	public PostgreSqlEntityFactory(EntityManager entityManager)
	{
		this.entityManager = requireNonNull(entityManager);
	}

	RowMapper<Entity> createRowMapper(EntityType entityType, Fetch fetch)
	{
		return new EntityMapper(entityManager, entityType, fetch);
	}

	Iterable<Entity> getReferences(EntityType refEntityType, Iterable<?> ids)
	{
		return entityManager.getReferences(refEntityType, ids);
	}

	private static class EntityMapper implements RowMapper<Entity>
	{
		private final EntityManager entityManager;
		private final EntityType entityType;
		private final Fetch fetch;

		private EntityMapper(EntityManager entityManager, EntityType entityType, Fetch fetch)
		{
			this.entityManager = requireNonNull(entityManager);
			this.entityType = requireNonNull(entityType);
			this.fetch = fetch; // can be null
		}

		@Override
		public Entity mapRow(ResultSet resultSet, int i) throws SQLException
		{
			Entity e = entityManager.createFetch(entityType, fetch);

			// TODO performance, iterate over fetch if available
			for (Attribute attr : entityType.getAtomicAttributes())
			{
				if (fetch == null || fetch.hasField(attr.getName()))
				{
					if (attr.getExpression() != null)
					{
						continue;
					}

					e.set(attr.getName(), mapValue(resultSet, attr));
				}
			}
			return e;
		}

		/**
		 * Maps a single results set value to an entity value.
		 * See the JDBC 4.0 specification appendix B titled "Data Type Conversion Tables" for conversion rules.
		 *
		 * @param resultSet result set
		 * @param attr      attribute
		 * @return value for the given attribute in the type defined by the attribute type
		 * @throws SQLException if an error occurs reading from the result set
		 */
		private Object mapValue(ResultSet resultSet, Attribute attr) throws SQLException
		{
			return mapValue(resultSet, attr, getColumnName(attr, false));
		}

		/**
		 * Maps a single results set value to an entity value.
		 * See the JDBC 4.0 specification appendix B titled "Data Type Conversion Tables" for conversion rules.
		 *
		 * @param resultSet result set
		 * @param attr      attribute
		 * @param colName   column name in the result set
		 * @return value for the given attribute in the type defined by the attribute type
		 * @throws SQLException if an error occurs reading from the result set
		 */
		private Object mapValue(ResultSet resultSet, Attribute attr, String colName) throws SQLException
		{
			try
			{
				Object value;
				switch (attr.getDataType())
				{
					case BOOL:
						boolean boolValue = resultSet.getBoolean(colName);
						value = resultSet.wasNull() ? null : boolValue;
						break;
					case CATEGORICAL:
					case FILE:
					case XREF:
						EntityType xrefEntityType = attr.getRefEntity();
						Object refIdValue = mapValue(resultSet, xrefEntityType.getIdAttribute(), colName);
						value = refIdValue != null ? entityManager.getReference(xrefEntityType, refIdValue) : null;
						break;
					case CATEGORICAL_MREF:
					case MREF:
						EntityType mrefEntityMeta = attr.getRefEntity();
						Array mrefArrayValue = resultSet.getArray(colName);
						value = resultSet.wasNull() ? null : mapValueMref(mrefArrayValue, mrefEntityMeta);
						break;
					case ONE_TO_MANY:
						Array oneToManyArrayValue = resultSet.getArray(colName);
						value = resultSet.wasNull() ? null : mapValueOneToMany(oneToManyArrayValue, attr);
						break;
					case COMPOUND:
						throw new RuntimeException(format("Value mapping not allowed for attribute type [%s]",
								attr.getDataType().toString()));
					case DATE:
						value = resultSet.getObject(colName, LocalDate.class);
						break;
					case DATE_TIME:
						OffsetDateTime offsetDateTime = resultSet.getObject(colName, OffsetDateTime.class);
						value = resultSet.wasNull() ? null : offsetDateTime.toInstant();
						break;
					case DECIMAL:
						BigDecimal bigDecimalValue = resultSet.getBigDecimal(colName);
						value = bigDecimalValue != null ? bigDecimalValue.doubleValue() : null;
						break;
					case EMAIL:
					case ENUM:
					case HTML:
					case HYPERLINK:
					case SCRIPT:
					case STRING:
					case TEXT:
						value = resultSet.getString(colName);
						break;
					case INT:
						int intValue = resultSet.getInt(colName);
						value = resultSet.wasNull() ? null : intValue;
						break;
					case LONG:
						long longValue = resultSet.getLong(colName);
						value = resultSet.wasNull() ? null : longValue;
						break;
					default:
						throw new UnexpectedEnumException(attr.getDataType());
				}
				return value;
			}
			catch (SQLException e)
			{
				throw e;
			}
		}

		/**
		 * Maps a single results set array value to an entity value for one-to-many attributes.
		 *
		 * @param arrayValue result set array value
		 * @param attr       attribute meta data
		 * @return mapped value
		 * @throws SQLException if an error occurs while attempting to access the array
		 */
		private Object mapValueOneToMany(Array arrayValue, Attribute attr) throws SQLException
		{
			EntityType entityType = attr.getRefEntity();
			Object value;
			Object[] postgreSqlMrefIds = (Object[]) arrayValue.getArray();
			if (postgreSqlMrefIds.length > 0 && postgreSqlMrefIds[0] != null)
			{
				Attribute idAttr = entityType.getIdAttribute();
				Object[] mrefIds = new Object[postgreSqlMrefIds.length];
				for (int i = 0; i < postgreSqlMrefIds.length; ++i)
				{
					Object mrefIdRaw = postgreSqlMrefIds[i];
					Object mrefId = mrefIdRaw != null ? convertMrefIdValue(mrefIdRaw.toString(), idAttr) : null;
					mrefIds[i] = mrefId;
				}

				// convert ids to (lazy) entities
				value = entityManager.getReferences(entityType, asList(mrefIds));
			}
			else
			{
				value = null;
			}
			return value;
		}

		/**
		 * Maps a single results set array value to an entity value for mref attributes.
		 *
		 * @param arrayValue result set array value
		 * @param entityType entity meta data
		 * @return mapped value
		 * @throws SQLException if an error occurs while attempting to access the array
		 */
		private Object mapValueMref(Array arrayValue, EntityType entityType) throws SQLException
		{
			// ResultSet contains a two dimensional array for MREF attribute values:
			// [[<order_nr_as_string>,<mref_id_as_string>],[<order_nr_as_string>,<mref_id_as_string>], ...]
			// In case there are no MREF attribute values the ResulSet is:
			// [[null,null]]
			Object value;
			String[][] mrefIdsAndOrder = (String[][]) arrayValue.getArray();
			if (mrefIdsAndOrder.length > 0 && mrefIdsAndOrder[0][0] != null)
			{
				Arrays.sort(mrefIdsAndOrder, comparing(o -> Integer.valueOf(o[0])));

				Attribute idAttr = entityType.getIdAttribute();
				Object[] mrefIds = new Object[mrefIdsAndOrder.length];
				for (int i = 0; i < mrefIdsAndOrder.length; ++i)
				{
					String[] mrefIdAndOrder = mrefIdsAndOrder[i];
					String mrefIdStr = mrefIdAndOrder[1];
					Object mrefId = mrefIdStr != null ? convertMrefIdValue(mrefIdStr, idAttr) : null;
					mrefIds[i] = mrefId;
				}

				// convert ids to (lazy) entities
				value = entityManager.getReferences(entityType, asList(mrefIds));
			}
			else
			{
				value = null;
			}
			return value;
		}

		/**
		 * Converts a mref id value string to an entity value.
		 *
		 * @param idValueStr id value string
		 * @param idAttr     id attribute
		 * @return entity value
		 */
		private static Object convertMrefIdValue(String idValueStr, Attribute idAttr)
		{
			// use iteration instead of tail recursion
			while (true)
			{
				AttributeType attrType = idAttr.getDataType();
				switch (attrType)
				{
					case BOOL:
						return Boolean.valueOf(idValueStr);
					case CATEGORICAL:
					case FILE:
					case XREF:
						idAttr = idAttr.getRefEntity().getIdAttribute();
						continue;
					case DATE:
						return parseLocalDate(idValueStr);
					case DATE_TIME:
						return parseInstant(idValueStr).atOffset(UTC);
					case DECIMAL:
						return Double.valueOf(idValueStr);
					case EMAIL:
					case ENUM:
					case HTML:
					case HYPERLINK:
					case SCRIPT:
					case STRING:
					case TEXT:
						return idValueStr;
					case INT:
						return Integer.valueOf(idValueStr);
					case LONG:
						return Long.valueOf(idValueStr);
					case CATEGORICAL_MREF:
					case COMPOUND:
					case MREF:
					case ONE_TO_MANY:
						throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
					default:
						throw new UnexpectedEnumException(attrType);
				}
			}
		}
	}
}
