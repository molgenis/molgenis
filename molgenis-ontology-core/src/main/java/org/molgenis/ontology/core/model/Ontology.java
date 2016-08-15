package org.molgenis.ontology.core.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Ontology.class)
public abstract class Ontology
{
	public abstract String getId();

	public abstract String getIRI();

	public abstract String getName();

	public static Ontology create(String id, String iri, String name)
	{
		return new AutoValue_Ontology(id, iri, name);
	}

}
