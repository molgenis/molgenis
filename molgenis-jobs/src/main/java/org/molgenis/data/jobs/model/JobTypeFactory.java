package org.molgenis.data.jobs.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobTypeFactory extends AbstractSystemEntityFactory<JobType, JobTypeMetadata, String>
{
	@Autowired
	JobTypeFactory(JobTypeMetadata jobTypeMetadata, EntityPopulator entityPopulator)
	{
		super(JobType.class, jobTypeMetadata, entityPopulator);
	}
}
