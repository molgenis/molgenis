package org.molgenis.data.index.job;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class IndexJobExecutionFactory
		extends AbstractSystemEntityFactory<IndexJobExecution, IndexJobExecutionMeta, String>
{
	IndexJobExecutionFactory(IndexJobExecutionMeta indexJobExecutionMeta, EntityPopulator entityPopulator)
	{
		super(IndexJobExecution.class, indexJobExecutionMeta, entityPopulator);
	}
}
