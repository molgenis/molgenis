package org.molgenis.dataexplorer.galaxy;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.GalaxyInstanceFactory;
import com.github.jmchilton.blend4j.galaxy.HistoriesClient;
import com.github.jmchilton.blend4j.galaxy.ToolsClient;
import com.github.jmchilton.blend4j.galaxy.ToolsClient.FileUploadRequest;
import com.github.jmchilton.blend4j.galaxy.ToolsClient.UploadFile;
import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.OutputDataset;
import com.github.jmchilton.blend4j.galaxy.beans.ToolExecution;

import java.io.File;
import java.util.List;

public class GalaxyDataExporter
{
	private static final String MOLGENIS_HISTORY_NAME = "MOLGENIS history";

	private final GalaxyInstance galaxyInstance;

	public GalaxyDataExporter(String galaxyUrl, String galaxyApiKey)
	{
		if (galaxyUrl == null) throw new IllegalArgumentException("galaxyUrl is null");
		if (galaxyApiKey == null) throw new IllegalArgumentException("galaxyApiKey is null");
		this.galaxyInstance = GalaxyInstanceFactory.get(galaxyUrl, galaxyApiKey);
	}

	public void export(String dataSetName, File tsvFile)
	{
		export(dataSetName, tsvFile, MOLGENIS_HISTORY_NAME);
	}

	public void export(String dataSetName, File tsvFile, String historyName) throws RuntimeException
	{
		try
		{
			// get/create history
			HistoriesClient historiesClient = galaxyInstance.getHistoriesClient();
			History molgenisHistory = null;
			for (History history : historiesClient.getHistories())
			{
				if (history.getName().equals(historyName))
				{
					molgenisHistory = history;
					break;
				}
			}
			if (molgenisHistory == null)
			{
				molgenisHistory = new History(historyName);
				molgenisHistory = historiesClient.create(molgenisHistory);
			}

			// upload data set file
			ToolsClient toolsClient = galaxyInstance.getToolsClient();
			UploadFile uploadFile = new UploadFile(tsvFile, dataSetName);
			FileUploadRequest fileUploadRequest = new FileUploadRequest(molgenisHistory.getId(), uploadFile);
			fileUploadRequest.setFileType("tabular");
			ToolExecution toolsExecution = toolsClient.upload(fileUploadRequest);
			List<OutputDataset> outputDatasets = toolsExecution.getOutputs();
			if (outputDatasets == null || outputDatasets.size() != 1)
			{
				throw new RuntimeException("Expected one output data set instead of " + outputDatasets.size());
			}
		}
		catch (RuntimeException e)
		{
			throw new RuntimeException(
					"An error occured while communicating with the Galaxy server. Please verify that the Galaxy server URL and API key are correct",
					e);
		}
	}
}
