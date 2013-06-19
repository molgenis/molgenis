package org.molgenis.dataexplorer.config;

import org.molgenis.dataexplorer.search.AsyncDataSetsIndexer;
import org.molgenis.dataexplorer.search.DataSetsIndexer;
import org.molgenis.dataexplorer.search.StartUpIndexer;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
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
	public StartUpIndexer startUpIndexer()
	{
		return new StartUpIndexer(dataSetsIndexer());
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
