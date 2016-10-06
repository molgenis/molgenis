package org.molgenis.ontology.core.model;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

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
