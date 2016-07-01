package org.molgenis.gavin.job;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexJobExecutionMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GavinJobExecutionFactory
		extends AbstractSystemEntityFactory<GavinJobExecution, ReindexJobExecutionMeta, String>
{
	@Autowired
	GavinJobExecutionFactory(ReindexJobExecutionMeta reindexJobExecutionMeta)
	{
		super(GavinJobExecution.class, reindexJobExecutionMeta);
	}
}