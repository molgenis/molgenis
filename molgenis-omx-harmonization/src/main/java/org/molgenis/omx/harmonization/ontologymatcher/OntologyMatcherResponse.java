package org.molgenis.omx.harmonization.ontologymatcher;

public class OntologyMatcherResponse
{
	private final Boolean isRunning;
	private final Integer matchePercentage;

	public OntologyMatcherResponse(Boolean isRunning, Integer matchePercentage)
	{
		this.isRunning = isRunning;
		this.matchePercentage = matchePercentage;
	}

	public Boolean isRunning()
	{
		return isRunning;
	}

	public Integer getMatchePercentage()
	{
		return matchePercentage;
	}
}