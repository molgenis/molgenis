package org.molgenis.data.meta.util;

import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;

/**
 * Wrapper for {@link Attribute#newInstance(Attribute, EntityType.AttributeCopyMode, AttributeFactory)} to improve testability.
 */
public interface AttributeCopier
{
	/**
	 * Returns a shallow copy of an attribute
	 *
	 * @param attribute attribute
	 * @return shallow copy of attribute
	 */
	Attribute copy(Attribute attribute);
}