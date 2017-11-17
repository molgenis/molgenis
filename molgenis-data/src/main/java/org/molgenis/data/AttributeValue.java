package org.molgenis.data;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.Attribute;

/**
 * Synonym: Cell
 */
@AutoValue
public abstract class AttributeValue
{
	public abstract Attribute getAttribute();

	public abstract Object getValue();

	public static AttributeValue create(Attribute attribute, Object value)
	{
		return new AutoValue_AttributeValue(attribute, value);
	}
}
