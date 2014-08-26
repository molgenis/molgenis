package org.molgenis.omx.biobankconnect.ontologymatcher;

import java.util.List;

public class AlgorithmGenerateResponse
{
	private final Integer targetDataSetId;
	private final List<Integer> selectedDataSetIds;
	private final Integer derivedDataSetId;
	private final String stage;
	private final Integer totalUsers;
	private final Boolean isRunning;
	private final Integer matchPercentage;

	public AlgorithmGenerateResponse(String stage, Boolean isRunning, Integer matchPercentage, Integer totalUsers,
			Integer targetDataSetId, List<Integer> selectedDataSetIds, Integer derivedDataSetId)
	{
		this.stage = stage;
		this.isRunning = isRunning;
		this.totalUsers = totalUsers;
		this.matchPercentage = matchPercentage;
		this.targetDataSetId = targetDataSetId;
		this.selectedDataSetIds = selectedDataSetIds;
		this.derivedDataSetId = derivedDataSetId;
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

	public Integer getTargetDataSet()
	{
		return targetDataSetId;
	}

	public List<Integer> getSelectedDataSets()
	{
		return selectedDataSetIds;
	}

	public Integer getDerivedDataSetId()
	{
		return derivedDataSetId;
	}
}