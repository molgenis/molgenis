package org.molgenis.data.elasticsearch.index.job;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class IndexJobExecutionFactory
		extends AbstractSystemEntityFactory<IndexJobExecution, IndexJobExecutionMeta, String>
{
	@Autowired
	IndexJobExecutionFactory(IndexJobExecutionMeta indexJobExecutionMeta, EntityPopulator entityPopulator)
	{
		super(IndexJobExecution.class, indexJobExecutionMeta, entityPopulator);
	}
}
