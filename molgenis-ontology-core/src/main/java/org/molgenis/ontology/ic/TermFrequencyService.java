package org.molgenis.ontology.ic;

public interface TermFrequencyService
{
	Double getTermFrequency(String term);

	Integer getTermOccurrence(String term);

	void updateTermFrequency();
}
