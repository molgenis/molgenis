package org.molgenis.dataexplorer.search;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Resource;

import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.omx.dataset.DataSetTable;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.search.SearchService;
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
	private SearchService searchService;
	private Database unauthorizedDatabase;
	private final AtomicInteger runningIndexProcesses = new AtomicInteger();

	@Autowired
	public void setSearchService(SearchService searchService)
	{
		this.searchService = searchService;
	}

	@Resource(name = "unauthorizedDatabase")
	public void setUnauthorizedDatabase(Database unauthorizedDatabase)
	{
		this.unauthorizedDatabase = unauthorizedDatabase;
	}

	@Override
	public void afterPropertiesSet() throws Exception
	{
		if (searchService == null) throw new IllegalArgumentException("Missing bean of type SearchService");
		if (unauthorizedDatabase == null) throw new IllegalArgumentException(
				"Missing bean of type Database with name 'unauthorizedDatabase'");
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
	public void index() throws DatabaseException, TableException
	{
		runningIndexProcesses.incrementAndGet();
		try
		{
			for (DataSet dataSet : unauthorizedDatabase.find(DataSet.class))
			{
				searchService.indexTupleTable(dataSet.getName(), new DataSetTable(dataSet, unauthorizedDatabase));
			}
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
	public void indexNew() throws DatabaseException, TableException
	{
		runningIndexProcesses.incrementAndGet();
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
		finally
		{
			runningIndexProcesses.decrementAndGet();
		}
	}

	@Override
	@Async
	public void index(List<DataSet> dataSets) throws TableException
	{
		runningIndexProcesses.incrementAndGet();
		try
		{
			for (DataSet dataSet : dataSets)
			{
				searchService.indexTupleTable(dataSet.getName(), new DataSetTable(dataSet, unauthorizedDatabase));
			}
		}
		finally
		{
			runningIndexProcesses.decrementAndGet();
		}
	}

}
