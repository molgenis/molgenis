package org.molgenis.omx.config;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.molgenis.omx.search.AsyncDataSetsIndexer;
import org.molgenis.omx.search.DataSetsIndexer;
import org.molgenis.omx.search.IndexingEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Exposes the DataSetsIndexer and schedules dataset indexing 4 am every night
 * 
 * @author erwin
 * 
 */
@Configuration
@EnableScheduling
@EnableAsync
public class DataExplorerConfig
{
	/**
	 * Get a reference to a DataSetsIndexer.
	 * 
	 * @return AsyncDataSetsIndexer
	 */
	@Bean
	public DataSetsIndexer dataSetsIndexer()
	{
		return new AsyncDataSetsIndexer();
	}

	/**
	 * Indexes not yet indexed DataSets at application startup, does not reindex already indexed DataSets (even not when
	 * there are chenges)
	 * 
	 * @return
	 */
	@Bean
	public IndexingEventListener startUpIndexer()
	{
		return new IndexingEventListener(dataSetsIndexer());
	}

	/**
	 * Indexes datasets at 4 am every night
	 * 
	 * @throws DatabaseException
	 * @throws TableException
	 */
	@Scheduled(cron = "0 0 4 * * ?")
	public void indexDataSets() throws DatabaseException, TableException
	{
		dataSetsIndexer().index();
	}
}
