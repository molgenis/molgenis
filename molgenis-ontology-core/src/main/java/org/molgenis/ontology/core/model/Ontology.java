package org.molgenis.ontology.core.model;

import org.molgenis.gson.AutoGson;
import org.molgenis.ontology.core.model.AutoValue_Ontology;

import com.google.auto.value.AutoValue;

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
