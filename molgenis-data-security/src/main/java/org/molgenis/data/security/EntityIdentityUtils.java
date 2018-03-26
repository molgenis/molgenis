package org.molgenis.data.security;

import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.util.UnexpectedEnumException;

/**
 * @see EntityIdentity
 */
public class EntityIdentityUtils
{
	private static final String TYPE_PREFIX = "entity";

	private EntityIdentityUtils()
	{
	}

	/**
	 * Returns the domain object type for the given entity type.
	 */
	public static String toType(EntityType entityType)
	{
		return toType(entityType.getId());
	}

	/**
	 * Returns the domain object type for the given entity type identifier.
	 */
	public static String toType(String entityTypeId)
	{
		return TYPE_PREFIX + '-' + entityTypeId;
	}

	/**
	 * Returns the domain object identifier type (e.g. String or Long) for the given entity type.
	 */
	public static Class<?> toIdType(EntityType entityType)
	{
		AttributeType attributeType = entityType.getIdAttribute().getDataType();
		//noinspection EnumSwitchStatementWhichMissesCases
		switch (attributeType)
		{
			case EMAIL:
			case HYPERLINK:
			case STRING:
				return String.class;
			case INT:
				return Integer.class;
			case LONG:
				return Long.class;
			default:
				throw new UnexpectedEnumException(attributeType);
		}
	}
}
