package org.molgenis.data.jobs.schedule;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.jobs.model.ScheduledJob;
import org.molgenis.data.jobs.model.ScheduledJobMetadata;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class ScheduledJobRepositoryDecoratorFactory
		extends AbstractSystemRepositoryDecoratorFactory<ScheduledJob, ScheduledJobMetadata>
{
	private final JobScheduler jobScheduler;
	private final DataService dataService;

	public ScheduledJobRepositoryDecoratorFactory(ScheduledJobMetadata fileIngestMetaData, JobScheduler jobScheduler,
			DataService dataService)
	{
		super(fileIngestMetaData);
		this.jobScheduler = requireNonNull(jobScheduler);
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public Repository<ScheduledJob> createDecoratedRepository(Repository<ScheduledJob> repository)
	{
		return new ScheduledJobRepositoryDecorator(repository, jobScheduler, dataService);
	}
}
