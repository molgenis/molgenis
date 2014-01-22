package org.molgenis.omx.biobankconnect.ontologymatcher;

public class OntologyMatcherResponse
{
	private final String stage;
	private final Integer totalUsers;
	private final Boolean isRunning;
	private final Integer matchPercentage;

	public OntologyMatcherResponse(String stage, Boolean isRunning, Integer matchPercentage, Integer totalUsers)
	{
		this.stage = stage;
		this.isRunning = isRunning;
		this.totalUsers = totalUsers;
		this.matchPercentage = matchPercentage;
	}

	public Boolean isRunning()
	{
		return isRunning;
	}

	public Integer getMatchePercentage()
	{
		return matchPercentage;
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