package org.molgenis.ontology.ic;

public interface TermFrequencyService
{
	/**
	 * Get the inverse document frequency of the given term
	 *
	 * @param term
	 * @return
	 */
	float getTermFrequency(String term);

	/**
	 * Get the occurrence of the given term
	 *
	 * @param term
	 * @return
	 */
	int getTermOccurrence(String term);

	/**
	 * Update the term frequency information
	 */
	void updateTermFrequency();
}
