package org.molgenis.jobs.schedule;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.validation.JsonValidator;
import org.molgenis.jobs.model.ScheduledJob;
import org.molgenis.jobs.model.ScheduledJobMetadata;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class ScheduledJobRepositoryDecoratorFactory
		extends AbstractSystemRepositoryDecoratorFactory<ScheduledJob, ScheduledJobMetadata>
{
	private final JobScheduler jobScheduler;
	private final JsonValidator jsonValidator;

	public ScheduledJobRepositoryDecoratorFactory(ScheduledJobMetadata scheduledJobMetadata, JobScheduler jobScheduler,
			JsonValidator jsonValidator)
	{
		super(scheduledJobMetadata);
		this.jobScheduler = requireNonNull(jobScheduler);
		this.jsonValidator = jsonValidator;
	}

	@Override
	public Repository<ScheduledJob> createDecoratedRepository(Repository<ScheduledJob> repository)
	{
		return new ScheduledJobRepositoryDecorator(repository, jobScheduler, jsonValidator);
	}
}
