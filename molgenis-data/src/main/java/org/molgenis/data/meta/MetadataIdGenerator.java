package org.molgenis.data.meta;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;

/**
 * Generator for human readable entity type and attribute identifiers.
 */
public interface MetadataIdGenerator
{
	/**
	 * Generates a human readable identifier for the given entity type.
	 *
	 * @param entityType entity type
	 * @return human readable entity type identifier
	 */
	String generateId(EntityType entityType);

	/**
	 * Generates a human readable identifier for the given attribute.
	 *
	 * @param attribute attribute
	 * @return human readable attribute identifier
	 */
	String generateId(Attribute attribute);
}
