package org.molgenis.model.registry.model;

import com.google.auto.value.AutoValue;

/**
 * @author sido
 */
@AutoValue
public abstract class ModelRegistryTag
{

	public static ModelRegistryTag create(String label, String iri, String relation)
	{
		return new AutoValue_ModelRegistryTag(label, iri, relation);
	}

	@SuppressWarnings("unused")
	public abstract String getLabel();

	@SuppressWarnings("unused")
	public abstract String getIri();

	@SuppressWarnings("unused")
	public abstract String getRelation();

}
