package org.molgenis.dataexplorer.search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.framework.tupletable.impl.MemoryTable;
import org.molgenis.omx.dataset.DataSetTable;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.search.SearchService;
import org.molgenis.util.DatabaseUtil;
import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.Tuple;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;

/**
 * Class that indexes all datasets for use in the dataexplorer async
 * 
 * @author erwin
 * 
 */
public class AsyncDataSetsIndexer implements DataSetsIndexer, InitializingBean
{
	private static final Logger LOG = Logger.getLogger(AsyncDataSetsIndexer.class);

	private SearchService searchService;
	private final AtomicInteger runningIndexProcesses = new AtomicInteger();

	@Autowired
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		if (searchService == null) throw new IllegalArgumentException("Missing bean of type SearchService");
	}

	@Override
	public boolean isIndexingRunning()
	{
		return (runningIndexProcesses.get() > 0);
	}

	/**
	 * Index all datasets
	 * 
	 * @throws DatabaseException
	 * @throws TableException
	 */
	@Override
	@Async
	public void index()
	{
		runningIndexProcesses.incrementAndGet();
		Database unauthorizedDatabase = DatabaseUtil.createDatabase();
		try
		{
			for (DataSet dataSet : unauthorizedDatabase.find(DataSet.class))
			{
				searchService.indexTupleTable(dataSet.getName(), new DataSetTable(dataSet, unauthorizedDatabase));
			}
		}
		catch (Exception e)
		{
			LOG.error("Exception index()", e);
		}
		finally
		{
			DatabaseUtil.closeQuietly(unauthorizedDatabase);
			runningIndexProcesses.decrementAndGet();
		}
	}

	/**
	 * Index all datatsets that are not in the index yet
	 * 
	 * @throws DatabaseException
	 * @throws TableException
	 */
	@Override
	@Async
	public void indexNew()
	{
		runningIndexProcesses.incrementAndGet();
		Database unauthorizedDatabase = DatabaseUtil.createDatabase();
		try
		{
			for (DataSet dataSet : unauthorizedDatabase.find(DataSet.class))
			{
				if (!searchService.documentTypeExists(dataSet.getName()))
				{
					searchService.indexTupleTable(dataSet.getName(), new DataSetTable(dataSet, unauthorizedDatabase));
				}
			}
		}
		catch (Exception e)
		{
			LOG.error("Exception index()", e);
		}
		finally
		{
			DatabaseUtil.closeQuietly(unauthorizedDatabase);
			runningIndexProcesses.decrementAndGet();
		}
	}

	@Override
	@Async
	public void index(List<DataSet> dataSets)
	{
		runningIndexProcesses.incrementAndGet();
		Database unauthorizedDatabase = DatabaseUtil.createDatabase();
		try
		{
			List<String> dataSetsName = new ArrayList<String>();
			for (DataSet dataSet : dataSets)
			{
				searchService.indexTupleTable(dataSet.getName(), new DataSetTable(dataSet, unauthorizedDatabase));
				dataSetsName.add(dataSet.getIdentifier());
			}

			List<DataSet> dataSetsUnauthorized = unauthorizedDatabase.find(DataSet.class, new QueryRule(
					DataSet.IDENTIFIER, Operator.IN, dataSetsName));

			for (DataSet dataSet : dataSetsUnauthorized)
			{
				// Store tree structure in protocol viewer
				searchService.indexTupleTable(
						"protocolViewer-" + dataSet.getName(),
						new MemoryTable(createTupleTableForTree("", dataSet.getName(), dataSet.getProtocolUsed(),
								unauthorizedDatabase)));
			}
		}
		catch (Exception e)
		{
			LOG.error("Exception index()", e);
		}
		finally
		{
			DatabaseUtil.closeQuietly(unauthorizedDatabase);
			runningIndexProcesses.decrementAndGet();
		}
	}

	public List<Tuple> createTupleTableForTree(String parentIdentifer, String dataSetName, Protocol protocolUsed,
			Database unauthorizedDatabase) throws DatabaseException
	{
		List<Tuple> listOfRows = new ArrayList<Tuple>();

		List<Protocol> subProtocols = protocolUsed.getSubprotocols();
		if (subProtocols.size() > 0)
		{
			for (Protocol p : subProtocols)
			{
				StringBuilder pathBuilder = new StringBuilder();
				KeyValueTuple tuple = new KeyValueTuple();
				tuple.set("id", p.getId());
				tuple.set("identifier", p.getIdentifier());
				tuple.set("name", p.getName().replaceAll("[^a-zA-Z0-9 ]", " "));
				tuple.set("type", "protocol");
				tuple.set("dataSet", dataSetName);
				tuple.set("description", p.getDescription() == null ? StringUtils.EMPTY : p.getDescription()
						.replaceAll("[^a-zA-Z0-9 ]", " "));
				if (!parentIdentifer.isEmpty()) pathBuilder.append(parentIdentifer).append('.');
				tuple.set("path", pathBuilder.append(p.getId()).toString());
				tuple.set("category", StringUtils.EMPTY);
				listOfRows.add(tuple);
				// recursively traverse down the tree
				listOfRows
						.addAll(createTupleTableForTree(pathBuilder.toString(), dataSetName, p, unauthorizedDatabase));
			}
		}
		else
		{
			List<ObservableFeature> listOfFeatures = unauthorizedDatabase.find(ObservableFeature.class, new QueryRule(
					ObservableFeature.ID, Operator.IN, protocolUsed.getFeatures_Id()));
			for (ObservableFeature feature : listOfFeatures)
			{
				StringBuilder pathBuilder = new StringBuilder();
				StringBuilder categoryValue = new StringBuilder();
				KeyValueTuple tuple = new KeyValueTuple();
				tuple.set("id", feature.getId());
				tuple.set("identifier", feature.getIdentifier());
				tuple.set("name", feature.getName().replaceAll("[^a-zA-Z0-9 ]", " "));
				tuple.set("type", "observablefeature");
				tuple.set("dataSet", dataSetName);
				tuple.set("description", feature.getDescription() == null ? StringUtils.EMPTY : feature
						.getDescription().replaceAll("[^a-zA-Z0-9 ]", " "));
				tuple.set("path", pathBuilder.append(parentIdentifer).append('.').append(feature.getId()).toString());
				for (Category c : getCategories(feature, unauthorizedDatabase))
				{
					categoryValue.append(
							c.getName() == null ? StringUtils.EMPTY : c.getName().replaceAll("[^a-zA-Z0-9 ]", " "))
							.append(' ');
				}
				tuple.set("category", categoryValue.toString());
				listOfRows.add(tuple);
			}
		}
		return listOfRows;
	}

	private List<Category> getCategories(ObservableFeature feature, Database unauthorizedDatabase)
			throws DatabaseException
	{
		List<Category> listOfValues = unauthorizedDatabase.find(Category.class, new QueryRule(
				Category.OBSERVABLEFEATURE_IDENTIFIER, Operator.EQUALS, feature.getIdentifier()));
		if (listOfValues == null) listOfValues = new ArrayList<Category>();
		return listOfValues;
	}
}
