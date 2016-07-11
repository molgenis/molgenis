package org.molgenis.data.postgresql;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityManager;
import org.molgenis.data.Fetch;
import org.molgenis.data.meta.model.AttributeMetaData;
import org.molgenis.data.meta.model.EntityMetaData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

@Component
public class PostgreSqlEntityFactory
{
	@SuppressWarnings("unused")
	private static final Logger LOG = LoggerFactory.getLogger(PostgreSqlEntityFactory.class);

	private final EntityManager entityManager;

	@Autowired
	public PostgreSqlEntityFactory(EntityManager entityManager)
	{
		this.entityManager = requireNonNull(entityManager);
	}

	RowMapper<Entity> createRowMapper(EntityMetaData entityMeta, Fetch fetch)
	{
		return new EntityMapper(entityManager, entityMeta, fetch);
	}

	Iterable<Entity> getReferences(EntityMetaData refEntityMeta, Iterable<?> ids)
	{
		return entityManager.getReferences(refEntityMeta, ids);
	}

	private static class EntityMapper implements RowMapper<Entity>
	{
		private final EntityManager entityManager;
		private final EntityMetaData entityMetaData;
		private final Fetch fetch;

		private EntityMapper(EntityManager entityManager, EntityMetaData entityMetaData, Fetch fetch)
		{
			this.entityManager = requireNonNull(entityManager);
			this.entityMetaData = requireNonNull(entityMetaData);
			this.fetch = fetch; // can be null
		}

		@Override
		public Entity mapRow(ResultSet resultSet, int i) throws SQLException
		{
			Entity e = entityManager.create(entityMetaData, fetch);

			// TODO performance, iterate over fetch if available
			for (AttributeMetaData attr : entityMetaData.getAtomicAttributes())
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
		 * @throws SQLException
		 */
		private Object mapValue(ResultSet resultSet, AttributeMetaData attr) throws SQLException
		{
			return mapValue(resultSet, attr, attr.getName());
		}

		/**
		 * Maps a single results set value to an entity value.
		 * See the JDBC 4.0 specification appendix B titled "Data Type Conversion Tables" for conversion rules.
		 *
		 * @param resultSet result set
		 * @param attr      attribute
		 * @param colName   column name in the result set
		 * @return value for the given attribute in the type defined by the attribute type
		 * @throws SQLException
		 */
		private Object mapValue(ResultSet resultSet, AttributeMetaData attr, String colName) throws SQLException
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
					EntityMetaData xrefEntityMeta = attr.getRefEntity();
					Object refIdValue = mapValue(resultSet, xrefEntityMeta.getIdAttribute(), colName);
					value = refIdValue != null ? entityManager.getReference(xrefEntityMeta, refIdValue) : null;
					break;
				case CATEGORICAL_MREF:
				case MREF:
					EntityMetaData mrefEntityMeta = attr.getRefEntity();
					Array arrayValue = resultSet.getArray(colName);
					value = resultSet.wasNull() ? null : mapValueMref(arrayValue, mrefEntityMeta);
					break;
				case COMPOUND:
					throw new RuntimeException(
							format("Value mapping not allowed for attribute type [%s]", attr.getDataType().toString()));
				case DATE:
					// valid, because java.sql.Date extends required type java.util.Date
					value = resultSet.getDate(colName);
					break;
				case DATE_TIME:
					// valid, because java.sql.Timestamp extends required type java.util.Date
					value = resultSet.getTimestamp(colName);
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
					throw new RuntimeException(format("Unknown attribute type [%s]", attr.getDataType().toString()));
			}
			return value;
		}

		/**
		 * Maps a single results set array value to an entity value for mref attributes.
		 *
		 * @param arrayValue result set array value
		 * @param entityMeta entity meta data
		 * @return mapped value
		 * @throws SQLException
		 */
		private Object mapValueMref(Array arrayValue, EntityMetaData entityMeta) throws SQLException
		{
			// ResultSet contains a two dimensional array for MREF attribute values:
			// [[<order_nr_as_string>,<mref_id_as_string>],[<order_nr_as_string>,<mref_id_as_string>], ...]
			// In case there are no MREF attribute values the ResulSet is:
			// [[null,null]]
			Object value;
			String[][] mrefIdsAndOrder = (String[][]) arrayValue.getArray();
			if (mrefIdsAndOrder.length > 0 && mrefIdsAndOrder[0][0] != null)
			{
				AttributeMetaData idAttr = entityMeta.getIdAttribute();
				Object[] mrefIds = new Object[mrefIdsAndOrder.length];
				for (String[] mrefIdAndOrder : mrefIdsAndOrder)
				{
					Integer seqNr = Integer.valueOf(mrefIdAndOrder[0]);
					String mrefIdStr = mrefIdAndOrder[1];
					Object mrefId = mrefIdStr != null ? convertMrefIdValue(mrefIdStr, idAttr) : null;
					mrefIds[seqNr] = mrefId;
				}

				// convert ids to (lazy) entities
				value = entityManager.getReferences(entityMeta, asList(mrefIds));
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
		private static Object convertMrefIdValue(String idValueStr, AttributeMetaData idAttr)
		{
			// use iteration instead of tail recursion
			while (true)
			{
				switch (idAttr.getDataType())
				{
					case BOOL:
						return Boolean.valueOf(idValueStr);
					case CATEGORICAL:
					case FILE:
					case XREF:
						idAttr = idAttr.getRefEntity().getIdAttribute();
						continue;
					case CATEGORICAL_MREF:
					case COMPOUND:
					case MREF:
						throw new RuntimeException(
								format("Invalid id attribute type [%s]", idAttr.getDataType().toString()));
					case DATE:
					case DATE_TIME:
						return Date.valueOf(idValueStr);
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
					default:
						throw new RuntimeException(
								format("Unknown attribute type [%s]", idAttr.getDataType().toString()));
				}
			}
		}
	}
}
