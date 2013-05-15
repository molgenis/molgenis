package org.molgenis.dataexplorer.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.dataexplorer.search.DataSetsIndexer;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.ui.PluginModel;
import org.molgenis.framework.ui.ScreenController;
import org.molgenis.framework.ui.ScreenMessage;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.Entity;
import org.molgenis.util.HandleRequestDelegationException;

/**
 * Plugin for indexing datasets. It shows a list of all dataset wich you can
 * choose to index.
 * 
 * @author erwin
 * 
 */
public class DataSetsIndexerPlugin extends PluginModel<Entity>
{
	private static final long serialVersionUID = 1L;
	private List<DataSet> dataSets = Collections.emptyList();

	public List<DataSet> getDataSets()
	{
		return dataSets;
	}

	public DataSetsIndexerPlugin(String name, ScreenController<?> parent)
	{
		super(name, parent);
	}

	@Override
	public String getViewTemplate()
	{
		return "templates/DataSetsIndexerPlugin.ftl";
	}

	@Override
	public String getViewName()
	{
		return "DataSetsIndexerPlugin";
	}

	@Override
	public void reload(Database db)
	{
		try
		{
			// Get all datasets
			dataSets = db.find(DataSet.class);
		}
		catch (DatabaseException e)
		{
			throw new RuntimeException(e);
		}
	}

	@Override
	public void handleRequest(Database db, MolgenisRequest request) throws HandleRequestDelegationException, Exception
	{
		// Get selected dataset id's from the request
		List<String> dataSetIds = request.getList("dataset");
		if ((dataSetIds == null) || dataSetIds.isEmpty())
		{
			setMessages(new ScreenMessage("Please select a dataset", false));
			return;
		}

		if (getDataSetsIndexer().isIndexingRunning())
		{
			setMessages(new ScreenMessage("Indexer is already running. Please wait until finished.", false));
			return;
		}

		// Convert the strings to integer
		List<Integer> ids = new ArrayList<Integer>();
		for (String dataSetId : dataSetIds)
		{
			if (StringUtils.isNumeric(dataSetId))
			{
				ids.add(Integer.parseInt(dataSetId));
			}
		}

		// Get the selected datasets from the databse and index them
		List<DataSet> dataSets = db.query(DataSet.class).in("id", ids).find();
		getDataSetsIndexer().index(dataSets);

		setMessages(new ScreenMessage("Indexing started", true));
	}

	/*
	 * Get the DataSetsIndexer bean from the spring context
	 */
	private DataSetsIndexer getDataSetsIndexer()
	{
		return ApplicationContextProvider.getApplicationContext().getBean(DataSetsIndexer.class);

	}
}
