package org.molgenis.data.jobs.model.hello;

import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.JobFactory;
import org.molgenis.data.jobs.model.ScheduledJobType;
import org.molgenis.data.jobs.model.ScheduledJobTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Import(HelloWorldService.class)
@Configuration
public class HelloWorldConfig
{
	@Autowired
	HelloWorldService helloWorldService;

	@Autowired
	ScheduledJobTypeFactory scheduledJobTypeFactory;

	@Autowired
	HelloWorldJobExecutionMetadata helloWorldJobExecutionMetadata;

	@Bean
	public JobFactory<HelloWorldJobExecution> helloWorldJobFactory()
	{
		return new JobFactory<HelloWorldJobExecution>()
		{
			@Override
			public Job createJob(HelloWorldJobExecution jobExecution)
			{
				final String who = jobExecution.getUser();
				final int delay = jobExecution.getDelay();
				return progress -> helloWorldService.helloWorld(progress, who, delay);
			}
		};
	}

	@Lazy
	@Bean
	public ScheduledJobType helloWorldJobType()
	{
		ScheduledJobType result = scheduledJobTypeFactory.create("helloWorld");
		result.setJobExecutionType(helloWorldJobExecutionMetadata);
		result.setLabel("Hello World");
		result.setDescription("Simple job example");
		result.setSchema("TODO! JSON schema goes here for parameter validation");
		return result;
	}
}
