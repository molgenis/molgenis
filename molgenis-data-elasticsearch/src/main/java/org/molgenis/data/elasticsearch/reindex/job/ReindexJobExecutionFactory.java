package org.molgenis.data.elasticsearch.reindex.job;

import org.molgenis.data.AbstractEntityFactory;
import org.molgenis.data.elasticsearch.reindex.meta.ReindexJobExecutionMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReindexJobExecutionFactory
		extends AbstractEntityFactory<ReindexJobExecution, ReindexJobExecutionMeta, String>
{
	@Autowired
	ReindexJobExecutionFactory(ReindexJobExecutionMeta reindexJobExecutionMeta)
	{
		super(ReindexJobExecution.class, reindexJobExecutionMeta, String.class);
	}
}
