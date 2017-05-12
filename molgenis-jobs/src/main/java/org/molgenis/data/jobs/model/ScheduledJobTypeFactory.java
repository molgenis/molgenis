package org.molgenis.data.jobs.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ScheduledJobTypeFactory extends AbstractSystemEntityFactory<ScheduledJobType, ScheduledJobTypeMetadata, String>
{
	@Autowired
	ScheduledJobTypeFactory(ScheduledJobTypeMetadata scheduledJobTypeMetadata, EntityPopulator entityPopulator)
	{
		super(ScheduledJobType.class, scheduledJobTypeMetadata, entityPopulator);
	}
}
