package org.molgenis.semanticmapper.job;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class MappingJobExecutionFactory
		extends AbstractSystemEntityFactory<MappingJobExecution, MappingJobExecutionMetadata, String>
{
	MappingJobExecutionFactory(MappingJobExecutionMetadata mappingJobExecutionMetadata, EntityPopulator entityPopulator)
	{
		super(MappingJobExecution.class, mappingJobExecutionMetadata, entityPopulator);
	}
}
