package org.molgenis.omx.biobankconnect.ontologymatcher;

public class OntologyMatcherResponse
{
	private final String stage;
	private final Integer totalUsers;
	private final Boolean isRunning;
	private final Integer matchePercentage;

	public OntologyMatcherResponse(String stage, Boolean isRunning, Integer matchePercentage, Integer totalUsers)
	{
		this.stage = stage;
		this.isRunning = isRunning;
		this.totalUsers = totalUsers;
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

	public String getStage()
	{
		return stage;
	}

	public Integer getTotalUsers()
	{
		return totalUsers;
	}
}