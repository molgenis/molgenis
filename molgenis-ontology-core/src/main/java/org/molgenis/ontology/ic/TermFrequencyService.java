package org.molgenis.ontology.ic;

public interface TermFrequencyService
{
	abstract Double getTermFrequency(String term);

	abstract Integer getTermOccurrence(String term);

	abstract void updateTermFrequency();
}
