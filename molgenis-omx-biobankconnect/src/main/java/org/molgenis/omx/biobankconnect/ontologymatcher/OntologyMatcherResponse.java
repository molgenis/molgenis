package org.molgenis.omx.biobankconnect.ontologymatcher;

public class OntologyMatcherResponse
{
	private final String stage;
	private final Boolean otherUsers;
	private final Boolean isRunning;
	private final Integer matchePercentage;

	public OntologyMatcherResponse(String stage, Boolean isRunning, Integer matchePercentage, Boolean otherUsers)
	{
		this.stage = stage;
		this.isRunning = isRunning;
		this.otherUsers = otherUsers;
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

	public Boolean getOtherUsers()
	{
		return otherUsers;
	}
}