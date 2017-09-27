package org.molgenis.data.postgresql.identifier;

import com.google.auto.value.AutoValue;
import org.molgenis.data.meta.model.Attribute;

/**
 * Contains just enough information about an attribute to create junction table names
 */
@AutoValue
public abstract class Identifiable
{
	public abstract String getName();

	public abstract String getId();

	public static Identifiable create(String name, String id)
	{
		return new AutoValue_Identifiable(name, id);
	}

	public static Identifiable create(Attribute attribute)
	{
		return create(attribute.getName(), attribute.getIdentifier());
	}
}
