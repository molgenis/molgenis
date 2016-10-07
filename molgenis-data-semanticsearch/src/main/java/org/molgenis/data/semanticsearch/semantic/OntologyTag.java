package org.molgenis.data.semanticsearch.semantic;

import com.google.auto.value.AutoValue;
import org.molgenis.data.semantic.Relation;
import org.molgenis.gson.AutoGson;
import org.molgenis.ontology.core.model.OntologyTagObject;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OntologyTag.class)
public abstract class OntologyTag
{
	public abstract OntologyTagObject getOntologyTagObject();

	public abstract String getRelationIRI();

	public static OntologyTag create(OntologyTagObject term, Relation relation)
	{
		return new AutoValue_OntologyTag(term, relation.getIRI());
	}
}
