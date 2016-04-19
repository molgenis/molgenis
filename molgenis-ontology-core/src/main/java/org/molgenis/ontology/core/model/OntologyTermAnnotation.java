package org.molgenis.ontology.core.model;

import org.molgenis.gson.AutoGson;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OntologyTermAnnotation.class)
public abstract class OntologyTermAnnotation
{
	public abstract String getName();

	public abstract String getValue();

	public static OntologyTermAnnotation create(String name, String value)
	{
		return new AutoValue_OntologyTermAnnotation(name, value);
	}
}
