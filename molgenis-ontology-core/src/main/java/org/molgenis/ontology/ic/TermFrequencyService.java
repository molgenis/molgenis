package org.molgenis.ontology.ic;

public interface TermFrequencyService
{
	public abstract float getTermFrequency(String term);

	public abstract Integer getTermOccurrence(String term);

	public abstract void updateTermFrequency();
}
