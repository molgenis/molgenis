package org.molgenis.dataexplorer.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.molgenis.dataexplorer.search.DataSetsIndexer;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
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

		// Convert the strings to integer
		List<Integer> ids = new ArrayList<Integer>();
		for (String dataSetId : dataSetIds)
		{
			if (StringUtils.isNumeric(dataSetId))
			{
				ids.add(Integer.parseInt(dataSetId));
			}
		}

		// Get the DataSetsIndexer bean from the spring context
		DataSetsIndexer indexer = ApplicationContextProvider.getApplicationContext().getBean(DataSetsIndexer.class);

		// Get the selected datasets from the databse and index them
		Query<DataSet> q = db.query(DataSet.class).in("id", ids);
		for (DataSet dataSet : q.find())
		{
			indexer.index(dataSet);
		}

		setMessages(new ScreenMessage("Indexing done", true));
	}
}
