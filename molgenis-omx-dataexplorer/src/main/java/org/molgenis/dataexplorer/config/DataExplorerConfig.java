package org.molgenis.dataexplorer.config;

import org.molgenis.dataexplorer.search.DataSetsIndexer;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.tupletable.TableException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Exposes the DataSetsIndexer and schedules dataset indexing 4 am every night
 * 
 * @author erwin
 * 
 */
@EnableScheduling
@Configuration
public class DataExplorerConfig
{
	@Bean
	public DataSetsIndexer dataSetsIndexer()
	{
		return new DataSetsIndexer();
	}

	// Index datasets at 4 am every night
	@Scheduled(cron = "0 0 4 * * ?")
	public void indexDataSets() throws DatabaseException, TableException
	{
		dataSetsIndexer().index();
	}
}
