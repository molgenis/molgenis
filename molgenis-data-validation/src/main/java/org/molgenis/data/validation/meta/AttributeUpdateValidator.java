package org.molgenis.data.validation.meta;

import org.molgenis.data.meta.model.Attribute;

/**
 * Validates {@link Attribute} updates.
 */
public interface AttributeUpdateValidator
{
	AttributeUpdateValidationResult validate(Attribute attribute, Attribute updatedAttribute);
}
