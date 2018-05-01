package org.molgenis.semanticsearch.semantic;

import com.google.auto.value.AutoValue;
import org.molgenis.data.semantic.Relation;
import org.molgenis.ontology.core.model.OntologyTerm;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OntologyTag.class)
public abstract class OntologyTag
{
	public abstract OntologyTerm getOntologyTerm();

	public abstract String getRelationIRI();

	public static OntologyTag create(OntologyTerm term, Relation relation)
	{
		return new AutoValue_OntologyTag(term, relation.getIRI());
	}
}
