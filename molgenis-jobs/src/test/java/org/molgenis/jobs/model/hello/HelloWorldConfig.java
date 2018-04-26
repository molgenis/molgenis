package org.molgenis.jobs.model.hello;

import com.google.gson.Gson;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.molgenis.jobs.model.ScheduledJobType;
import org.molgenis.jobs.model.ScheduledJobTypeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Lazy;

import static com.google.common.collect.ImmutableMap.of;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Import(HelloWorldService.class)
@Configuration
public class HelloWorldConfig
{
	private HelloWorldService helloWorldService;
	private ScheduledJobTypeFactory scheduledJobTypeFactory;
	private HelloWorldJobExecutionMetadata helloWorldJobExecutionMetadata;
	private Gson gson;

	public HelloWorldConfig(HelloWorldService helloWorldService, ScheduledJobTypeFactory scheduledJobTypeFactory,
			HelloWorldJobExecutionMetadata helloWorldJobExecutionMetadata, Gson gson)
	{
		this.helloWorldService = requireNonNull(helloWorldService);
		this.scheduledJobTypeFactory = requireNonNull(scheduledJobTypeFactory);
		this.helloWorldJobExecutionMetadata = requireNonNull(helloWorldJobExecutionMetadata);
		this.gson = requireNonNull(gson);
	}

	@Bean
	public JobFactory<HelloWorldJobExecution> helloWorldJobFactory()
	{
		return new JobFactory<HelloWorldJobExecution>()
		{
			@Override
			public Job<String> createJob(HelloWorldJobExecution jobExecution)
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
		String schema = gson.toJson(
				of("title", "Hello World Job", "type", "object", "properties", of("delay", of("type", "integer")),
						"required", singletonList("delay")));
		result.setSchema(schema);
		return result;
	}
}
