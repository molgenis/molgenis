package org.molgenis.ontology.core.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OntologyTermChildrenCacheKey.class)
public abstract class OntologyTermChildrenCacheKey
{
	public abstract OntologyTerm getOntologyTerm();

	public abstract int getMaxLevel();

	public static OntologyTermChildrenCacheKey create(OntologyTerm ontologyTerm, int maxLevel)
	{
		return new AutoValue_OntologyTermChildrenCacheKey(ontologyTerm, maxLevel);
	}
}
