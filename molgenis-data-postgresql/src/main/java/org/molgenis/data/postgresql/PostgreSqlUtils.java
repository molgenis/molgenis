package org.molgenis.data.postgresql;

import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.util.UnexpectedEnumException;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import static java.lang.String.format;
import static java.time.ZoneOffset.UTC;
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
				return stream(entities.spliterator(), false).map(
						mrefEntity -> getPostgreSqlValue(mrefEntity, mrefEntity.getEntityType().getIdAttribute()))
															.collect(toList());
			case DATE:
				return entity.getLocalDate(attrName);
			case DATE_TIME:
				// As a workaround for #5674, we don't store milliseconds
				Instant instant = entity.getInstant(attrName);
				return instant != null ? instant.truncatedTo(ChronoUnit.SECONDS).atOffset(UTC) : null;
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
				throw new UnexpectedEnumException(attrType);
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
					if (queryValue != null && !(queryValue instanceof LocalDate))
					{
						throw new MolgenisDataException(
								format("Attribute [%s] query value is of type [%s] instead of [%s]", attrName,
										queryValue.getClass().getSimpleName(), LocalDate.class.getSimpleName()));
					}
					return queryValue;
				case DATE_TIME:
					if (queryValue == null)
					{
						return null;
					}
					if (!(queryValue instanceof Instant))
					{
						throw new MolgenisDataException(
								format("Attribute [%s] query value is of type [%s] instead of [%s]", attrName,
										queryValue.getClass().getSimpleName(), Instant.class.getSimpleName()));
					}
					return ((Instant) queryValue).atOffset(UTC);
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
					throw new UnexpectedEnumException(attrType);
			}
		}
	}
}
