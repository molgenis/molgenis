package org.molgenis.ontology.ic;

public class PubMedTFEntity
{
	private final int occurrence;
	private final double frequency;

	public PubMedTFEntity(int occurrence, double frequency)
	{
		this.occurrence = occurrence;
		this.frequency = frequency;
	}

	public int getOccurrence()
	{
		return occurrence;
	}

	public double getFrequency()
	{
		return frequency;
	}
}
