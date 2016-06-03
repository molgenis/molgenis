package org.molgenis.data.elasticsearch.reindex;

import org.molgenis.data.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReindexActionRegisterConfig
{
	@Autowired
	private DataService dataService;

	@Autowired
	private ReindexActionJobMetaData reindexActionJobMetaData;

	@Autowired
	private ReindexActionMetaData reindexActionMetaData;

	@Bean
	public ReindexActionRegisterService reindexActionRegisterService()
	{
		return new ReindexActionRegisterService(dataService, reindexActionJobMetaData, reindexActionMetaData);
	}
}
