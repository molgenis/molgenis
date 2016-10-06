package org.molgenis.ontology.core.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_ChildrenRetrievalParam.class)
public abstract class ChildrenRetrievalParam
{
	public abstract OntologyTermImpl getOntologyTermImpl();

	public abstract int getMaxLevel();

	public static ChildrenRetrievalParam create(OntologyTermImpl ontologyTermImpl, int maxLevel)
	{
		return new AutoValue_ChildrenRetrievalParam(ontologyTermImpl, maxLevel);
	}
}
