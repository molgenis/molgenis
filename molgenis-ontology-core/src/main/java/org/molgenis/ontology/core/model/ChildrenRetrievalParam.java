package org.molgenis.ontology.core.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ChildrenRetrievalParam.class)
public abstract class ChildrenRetrievalParam
{
	public abstract OntologyTerm getOntologyTerm();

	public abstract int getMaxLevel();

	public static ChildrenRetrievalParam create(OntologyTerm ontologyTerm, int maxLevel)
	{
		return new AutoValue_ChildrenRetrievalParam(ontologyTerm, maxLevel);
	}
}
