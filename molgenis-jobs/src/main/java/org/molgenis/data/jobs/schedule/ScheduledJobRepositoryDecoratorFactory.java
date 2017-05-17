package org.molgenis.data.jobs.schedule;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.jobs.model.ScheduledJob;
import org.molgenis.data.jobs.model.ScheduledJobMetadata;
import org.molgenis.data.validation.JsonValidator;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class ScheduledJobRepositoryDecoratorFactory
		extends AbstractSystemRepositoryDecoratorFactory<ScheduledJob, ScheduledJobMetadata>
{
	private final JobScheduler jobScheduler;
	private final DataService dataService;
	private final JsonValidator jsonValidator;

	public ScheduledJobRepositoryDecoratorFactory(ScheduledJobMetadata fileIngestMetaData, JobScheduler jobScheduler,
			DataService dataService, JsonValidator jsonValidator)
	{
		super(fileIngestMetaData);
		this.jobScheduler = requireNonNull(jobScheduler);
		this.dataService = requireNonNull(dataService);
		this.jsonValidator = jsonValidator;
	}

	@Override
	public Repository<ScheduledJob> createDecoratedRepository(Repository<ScheduledJob> repository)
	{
		return new ScheduledJobRepositoryDecorator(repository, jobScheduler, jsonValidator);
	}
}
