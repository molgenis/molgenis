package org.molgenis.data.semanticsearch.explain.bean;

import java.util.Map;

import org.molgenis.gson.AutoGson;
import org.molgenis.ontology.core.model.OntologyTerm;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_OntologyTermQueryExpansionSolution.class)
public abstract class OntologyTermQueryExpansionSolution implements Comparable<OntologyTermQueryExpansionSolution>
{
	public abstract Map<OntologyTerm, OntologyTerm> getMatchOntologyTerms();

	public abstract boolean isHighQuality();

	public static OntologyTermQueryExpansionSolution create(Map<OntologyTerm, OntologyTerm> matchOntologyTerms,
			boolean highQuality)
	{
		return new AutoValue_OntologyTermQueryExpansionSolution(matchOntologyTerms, highQuality);
	}

	public int compareTo(OntologyTermQueryExpansionSolution other)
	{
		return Integer.compare(other.getMatchOntologyTerms().size(), getMatchOntologyTerms().size());
	}
}
