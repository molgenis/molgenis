package org.molgenis.data.elasticsearch.reindex;

import org.molgenis.data.reindex.ReindexActionRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static org.molgenis.data.elasticsearch.reindex.meta.ReindexJobExecutionMeta.REINDEX_JOB_EXECUTION;

@Configuration
public class ReindexConfig
{
	@Autowired
	private ReindexActionRegisterService reindexActionRegisterService;

	@PostConstruct
	public void register()
	{
		reindexActionRegisterService.addExcludedEntity(REINDEX_JOB_EXECUTION);
	}

}
