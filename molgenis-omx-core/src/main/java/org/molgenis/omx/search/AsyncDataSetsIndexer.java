package org.molgenis.omx.search;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.omx.dataset.DataSetTable;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.protocol.ProtocolTable;
import org.molgenis.search.SearchService;
import org.molgenis.security.runas.RunAsSystem;
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

	@Autowired
	private DataService dataService;

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
	@RunAsSystem
	public void index()
	{
		runningIndexProcesses.incrementAndGet();
		try
		{
			Iterable<DataSet> dataSets = dataService.findAll(DataSet.ENTITY_NAME, new QueryImpl());
			for (DataSet dataSet : dataSets)
			{
				searchService.indexTupleTable(dataSet.getIdentifier(), new DataSetTable(dataSet, dataService));
				searchService.indexTupleTable("protocolTree-" + dataSet.getId(),
						new ProtocolTable(dataSet.getProtocolUsed(), dataService));
			}
		}
		catch (Exception e)
		{
			LOG.error("Exception index()", e);
		}
		finally
		{
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
	@RunAsSystem
	public void indexNew()
	{
		List<Integer> dataSetIds = new ArrayList<Integer>();

		try
		{
			Iterable<DataSet> dataSets = dataService.findAll(DataSet.ENTITY_NAME, new QueryImpl());
			for (DataSet dataSet : dataSets)
			{
				if (!searchService.documentTypeExists(dataSet.getIdentifier()))
				{
					dataSetIds.add(dataSet.getId());
				}
			}
		}
		catch (Exception e)
		{
			LOG.error("Exception index()", e);
		}

		if (!dataSetIds.isEmpty())
		{
			index(dataSetIds);
		}
	}

	@Override
	@Async
	@RunAsSystem
	public void index(List<Integer> dataSetIds)
	{
		while (isIndexingRunning())
		{
			try
			{
				Thread.sleep(5000);
			}
			catch (InterruptedException e)
			{
				throw new RuntimeException(e);
			}
		}

		runningIndexProcesses.incrementAndGet();
		try
		{
			Iterable<DataSet> dataSets = dataService.findAll(DataSet.ENTITY_NAME, dataSetIds);

			for (DataSet dataSet : dataSets)
			{
				searchService.indexTupleTable(dataSet.getIdentifier(), new DataSetTable(dataSet, dataService));
				searchService.indexTupleTable("protocolTree-" + dataSet.getId(),
						new ProtocolTable(dataSet.getProtocolUsed(), dataService));
			}
		}
		catch (Exception e)
		{
			LOG.error("Exception index()", e);
		}
		finally
		{
			runningIndexProcesses.decrementAndGet();
		}
	}

}
