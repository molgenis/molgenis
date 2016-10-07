package org.molgenis.ontology.core.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_SemanticType.class)
public abstract class SemanticType
{
	public abstract String getIdentifier();

	public abstract String getName();

	public abstract String getGroup();

	public abstract boolean isGlobalKeyConcept();

	public static SemanticType create(String identifier, String name, String group, boolean globalKeyConcept)
	{
		return new AutoValue_SemanticType(identifier, name, group, globalKeyConcept);
	}
}
