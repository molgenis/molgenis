package org.molgenis.data.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;
import org.molgenis.ontology.core.model.OntologyTerm;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OntologyTermHit.class)
public abstract class OntologyTermHit
{
	public abstract OntologyTerm getOntologyTerm();

	public abstract String getMatchedWords();

	public abstract float getScore();

	public static OntologyTermHit create(OntologyTerm ontologyTerm, String matchedWords, float score)
	{
		return new AutoValue_OntologyTermHit(ontologyTerm, matchedWords, score);
	}
}
