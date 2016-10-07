package org.molgenis.data.semanticsearch.explain.bean;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;
import org.molgenis.ontology.core.model.OntologyTerm;

import java.util.Map;

@AutoValue
@AutoGson(autoValueClass = AutoValue_QueryExpansionSolution.class)
public abstract class QueryExpansionSolution implements Comparable<QueryExpansionSolution>
{
	public static QueryExpansionSolution create(Map<OntologyTerm, OntologyTerm> matchOntologyTerms, float percentage,
			boolean highQuality)
	{
		return new AutoValue_QueryExpansionSolution(matchOntologyTerms, percentage, highQuality);
	}

	public abstract Map<OntologyTerm, OntologyTerm> getMatchOntologyTerms();

	public abstract float getPercentage();

	public abstract boolean isHighQuality();

	public int compareTo(QueryExpansionSolution other)
	{
		return Float.compare(other.getPercentage(), getPercentage());
	}
}
