package org.molgenis.omx.search;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.omx.dataset.DataSetMatrixRepository;
import org.molgenis.omx.observ.DataSet;
import org.molgenis.omx.observ.Protocol;
import org.molgenis.omx.protocol.CategoryRepository;
import org.molgenis.omx.protocol.ProtocolTreeRepository;
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
	private final AtomicInteger runningIndexProcesses = new AtomicInteger();
	@Autowired
	private DataService dataService;
	private SearchService searchService;

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
				searchService.indexRepository(new DataSetMatrixRepository(dataService, dataSet.getIdentifier()));
				searchService.indexRepository(new ProtocolTreeRepository(dataSet.getProtocolUsed(), dataService,
						"protocolTree-" + dataSet.getId()));
				searchService.indexRepository(new CategoryRepository(dataSet.getProtocolUsed(), dataSet.getId(),
						dataService));
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

	@Override
	@Async
	@RunAsSystem
	public void indexDataSets(List<Integer> dataSetIds)
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
				searchService.indexRepository(new DataSetMatrixRepository(dataService, dataSet.getIdentifier()));
				searchService.indexRepository(new ProtocolTreeRepository(dataSet.getProtocolUsed(), dataService,
						"protocolTree-" + dataSet.getId()));
				searchService.indexRepository(new CategoryRepository(dataSet.getProtocolUsed(), dataSet.getId(),
						dataService));
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

	@Override
	@Async
	@RunAsSystem
	public void indexProtocols(List<Integer> protocolIds)
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
			Iterable<Protocol> protocols = dataService.findAll(Protocol.ENTITY_NAME, protocolIds);

			for (Protocol protocol : protocols)
			{
				searchService.indexRepository(new ProtocolTreeRepository(protocol, dataService, "protocolTree-"
						+ protocol.getId()));
				searchService.indexRepository(new CategoryRepository(protocol, protocol.getId(), dataService));
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
