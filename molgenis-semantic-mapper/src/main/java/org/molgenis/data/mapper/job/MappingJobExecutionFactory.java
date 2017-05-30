package org.molgenis.data.mapper.job;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MappingJobExecutionFactory
		extends AbstractSystemEntityFactory<MappingJobExecution, MappingJobExecutionMetadata, String>
{
	@Autowired
	MappingJobExecutionFactory(MappingJobExecutionMetadata mappingJobExecutionMetadata, EntityPopulator entityPopulator)
	{
		super(MappingJobExecution.class, mappingJobExecutionMetadata, entityPopulator);
	}
}
