package org.molgenis.ontology.sorta.bean;

import org.molgenis.gson.AutoGson;
import org.molgenis.ontology.core.model.OntologyTerm;

import com.google.auto.value.AutoValue;

@AutoValue
@AutoGson(autoValueClass = AutoValue_SortaHit.class)
public abstract class SortaHit implements Comparable<SortaHit>
{
	public abstract OntologyTerm getOntologyTerm();

	public abstract double getScore();

	public abstract double getWeightedScore();

	public static SortaHit create(OntologyTerm ontologyTerm, double score, double weightedScore)
	{
		return new AutoValue_SortaHit(ontologyTerm, score, weightedScore);
	}

	@Override
	public int compareTo(SortaHit other)
	{
		return Double.compare(other.getWeightedScore(), getWeightedScore());
	}
}
