package org.molgenis.data;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.Attribute;

import javax.annotation.Nullable;

/**
 * Synonym: Cell
 */
@AutoValue
public abstract class AttributeValue
{
	public abstract Attribute getAttribute();

	@Nullable
	public abstract Object getValue();

	public static AttributeValue create(Attribute attribute, @Nullable Object value)
	{
		return new AutoValue_AttributeValue(attribute, value);
	}
}
