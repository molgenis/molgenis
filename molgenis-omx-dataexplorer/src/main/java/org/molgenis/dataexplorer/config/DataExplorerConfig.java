package org.molgenis.dataexplorer.config;

import org.molgenis.dataexplorer.search.DataSetsIndexer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataExplorerConfig
{
	@Bean
	public DataSetsIndexer dataSetsIndexer()
	{
		return new DataSetsIndexer();
	}
}
