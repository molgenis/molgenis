package org.molgenis.data.postgresql;

import org.apache.commons.lang3.time.DateUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.file.model.FileMeta;

import java.time.ZoneId;
import java.util.Date;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

/**
 * PostgreSQL utilities such as entity value to PostgreSQL value conversion.
 */
class PostgreSqlUtils
{
	private PostgreSqlUtils()
	{
	}

	/**
	 * Returns the PostgreSQL value for the given entity attribute
	 *
	 * @param entity entity
	 * @param attr   attribute
	 * @return PostgreSQL value
	 */
	static Object getPostgreSqlValue(Entity entity, Attribute attr)
	{
		String attrName = attr.getName();
		AttributeType attrType = attr.getDataType();

		switch (attrType)
		{
			case BOOL:
				return entity.getBoolean(attrName);
			case CATEGORICAL:
			case XREF:
				Entity xrefEntity = entity.getEntity(attrName);
				return xrefEntity != null ? getPostgreSqlValue(xrefEntity,
						xrefEntity.getEntityType().getIdAttribute()) : null;
			case CATEGORICAL_MREF:
			case MREF:
			case ONE_TO_MANY:
				Iterable<Entity> entities = entity.getEntities(attrName);
				return stream(entities.spliterator(), false).map(mrefEntity -> getPostgreSqlValue(mrefEntity,
						mrefEntity.getEntityType().getIdAttribute())).collect(toList());
			case DATE:
				Date date = entity.getUtilDate(attrName);
				// http://stackoverflow.com/questions/21242110/convert-java-util-date-to-java-time-localdate
				return date != null ? date.toInstant().atZone(ZoneId.of("UTC")).toLocalDate() : null;
			case DATE_TIME:
				Date dateTime = entity.getUtilDate(attrName);
				return dateTime != null ? new java.sql.Timestamp(
						(dateTime.getTime() / DateUtils.MILLIS_PER_SECOND) * DateUtils.MILLIS_PER_SECOND) : null;
			case DECIMAL:
				return entity.getDouble(attrName);
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case SCRIPT:
			case STRING:
			case TEXT:
				return entity.getString(attrName);
			case FILE:
				FileMeta fileEntity = entity.getEntity(attrName, FileMeta.class);
				return fileEntity != null ? getPostgreSqlValue(fileEntity,
						fileEntity.getEntityType().getIdAttribute()) : null;
			case INT:
				return entity.getInt(attrName);
			case LONG:
				return entity.getLong(attrName);
			case COMPOUND:
				throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
			default:
				throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
		}
	}

	/**
	 * Returns the PostgreSQL query value for the given entity attribute. For query operators requiring a list of
	 * values (e.g. IN or RANGE) this method must be called for each individual query value.
	 *
	 * @param queryValue value of the type that matches the attribute type
	 * @param attr       attribute
	 * @return PostgreSQL value
	 */
	static Object getPostgreSqlQueryValue(Object queryValue, Attribute attr)
	{
		while (true)
		{
			String attrName = attr.getName();
			AttributeType attrType = attr.getDataType();

			switch (attrType)
			{
				case BOOL:
					if (queryValue != null && !(queryValue instanceof Boolean))
					{
						throw new MolgenisDataException(
								format("Attribute [%s] query value is of type [%s] instead of [%s]", attrName,
										queryValue.getClass().getSimpleName(), Boolean.class.getSimpleName()));
					}
					return queryValue;
				case CATEGORICAL:
				case CATEGORICAL_MREF: // one query value
				case FILE:
				case MREF: // one query value
				case XREF:
				case ONE_TO_MANY:
					// queries values referencing an entity can either be the entity itself or the entity id
					if (queryValue != null)
					{
						if (queryValue instanceof Entity)
						{
							queryValue = ((Entity) queryValue).getIdValue();
						}
						attr = attr.getRefEntity().getIdAttribute();
						continue;
					}
					else
					{
						return null;
					}
				case DATE:
					if (queryValue != null && !(queryValue instanceof Date))
					{
						throw new MolgenisDataException(
								format("Attribute [%s] query value is of type [%s] instead of [%s]", attrName,
										queryValue.getClass().getSimpleName(), Date.class.getSimpleName()));
					}
					Date date = (Date) queryValue;
					return date != null ? new java.sql.Date(date.getTime()) : null;
				case DATE_TIME:
					if (queryValue != null && !(queryValue instanceof Date))
					{
						throw new MolgenisDataException(
								format("Attribute [%s] query value is of type [%s] instead of [%s]", attrName,
										queryValue.getClass().getSimpleName(), Date.class.getSimpleName()));
					}
					Date dateTime = (Date) queryValue;
					return dateTime != null ? new java.sql.Timestamp(dateTime.getTime()) : null;
				case DECIMAL:
					if (queryValue != null && !(queryValue instanceof Double))
					{
						throw new MolgenisDataException(
								format("Attribute [%s] query value is of type [%s] instead of [%s]", attrName,
										queryValue.getClass().getSimpleName(), Double.class.getSimpleName()));
					}
					return queryValue;
				case ENUM:
					// enum query values can be an enum or enum string
					if (queryValue != null)
					{
						if (queryValue instanceof String)
						{
							return queryValue;
						}
						else if (queryValue instanceof Enum<?>)
						{
							return queryValue.toString();
						}
						else
						{
							throw new MolgenisDataException(
									format("Attribute [%s] query value is of type [%s] instead of [%s] or [%s]",
											attrName, queryValue.getClass().getSimpleName(),
											String.class.getSimpleName(), Enum.class.getSimpleName()));
						}
					}
					else
					{
						return null;
					}
				case EMAIL:
				case HTML:
				case HYPERLINK:
				case SCRIPT:
				case STRING:
				case TEXT:
					if (queryValue != null && !(queryValue instanceof String))
					{
						throw new MolgenisDataException(
								format("Attribute [%s] query value is of type [%s] instead of [%s]", attrName,
										queryValue.getClass().getSimpleName(), String.class.getSimpleName()));
					}
					return queryValue;
				case INT:
					if (queryValue != null && !(queryValue instanceof Integer))
					{
						throw new MolgenisDataException(
								format("Attribute [%s] query value is of type [%s] instead of [%s]", attrName,
										queryValue.getClass().getSimpleName(), Integer.class.getSimpleName()));
					}
					return queryValue;
				case LONG:
					if (queryValue != null && !(queryValue instanceof Long))
					{
						throw new MolgenisDataException(
								format("Attribute [%s] query value is of type [%s] instead of [%s]", attrName,
										queryValue.getClass().getSimpleName(), Long.class.getSimpleName()));
					}
					return queryValue;
				case COMPOUND:
					throw new RuntimeException(format("Illegal attribute type [%s]", attrType.toString()));
				default:
					throw new RuntimeException(format("Unknown attribute type [%s]", attrType.toString()));
			}
		}
	}
}
