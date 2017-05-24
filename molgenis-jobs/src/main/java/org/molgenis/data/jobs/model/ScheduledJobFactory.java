package org.molgenis.data.jobs.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScheduledJobFactory extends AbstractSystemEntityFactory<ScheduledJob, ScheduledJobMetadata, String>
{
	@Autowired
	ScheduledJobFactory(ScheduledJobMetadata scheduledJobMetadata, EntityPopulator entityPopulator)
	{
		super(ScheduledJob.class, scheduledJobMetadata, entityPopulator);
	}
}
