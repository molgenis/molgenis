package org.molgenis.jobs.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class ScheduledJobTypeFactory
		extends AbstractSystemEntityFactory<ScheduledJobType, ScheduledJobTypeMetadata, String>
{
	ScheduledJobTypeFactory(ScheduledJobTypeMetadata scheduledJobTypeMetadata, EntityPopulator entityPopulator)
	{
		super(ScheduledJobType.class, scheduledJobTypeMetadata, entityPopulator);
	}
}
