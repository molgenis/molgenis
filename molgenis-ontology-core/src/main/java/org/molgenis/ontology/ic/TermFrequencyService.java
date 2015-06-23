package org.molgenis.ontology.ic;

public interface TermFrequencyService
{
	abstract Double getTermFrequency(String term);

	abstract void updateTermFrequency();
}
