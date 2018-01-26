package org.molgenis.jobs.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class ScheduledJobFactory extends AbstractSystemEntityFactory<ScheduledJob, ScheduledJobMetadata, String>
{
	ScheduledJobFactory(ScheduledJobMetadata scheduledJobMetadata, EntityPopulator entityPopulator)
	{
		super(ScheduledJob.class, scheduledJobMetadata, entityPopulator);
	}
}
