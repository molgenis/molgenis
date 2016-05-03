package org.molgenis.data.elasticsearch.reindex.meta;

import org.molgenis.data.DataService;
import org.molgenis.data.elasticsearch.reindex.ReindexActionRegisterService;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionJobMetaData;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexActionMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReindexActionRegisterConfig
{
	@Autowired
	private DataService dataService;

	public static final String BACKEND = "PostgreSQL";

	@Bean
	public ReindexActionJobMetaData reindexActionJobMetaData()
	{
		return new ReindexActionJobMetaData(BACKEND);
	}

	@Bean
	public ReindexActionMetaData reindexActionMetaData()
	{
		return new ReindexActionMetaData(reindexActionJobMetaData(), BACKEND);
	}

	@Bean
	public ReindexActionRegisterService reindexActionRegisterService()
	{
		return new ReindexActionRegisterService(dataService, reindexActionJobMetaData(),
 reindexActionMetaData());
	}
}
