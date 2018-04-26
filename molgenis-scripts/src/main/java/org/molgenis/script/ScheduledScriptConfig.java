package org.molgenis.script;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.molgenis.jobs.model.ScheduledJobType;
import org.molgenis.jobs.model.ScheduledJobTypeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static com.google.common.collect.ImmutableMap.of;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
public class ScheduledScriptConfig
{
	private static final Type MAP_TOKEN = new TypeToken<Map<String, Object>>()
	{
	}.getType();

	private final SavedScriptRunner savedScriptRunner;
	private final ScheduledJobTypeFactory scheduledJobTypeFactory;
	private final ScriptJobExecutionMetadata scriptJobExecutionMetadata;
	private final Gson gson;

	public ScheduledScriptConfig(SavedScriptRunner savedScriptRunner, ScheduledJobTypeFactory scheduledJobTypeFactory,
			ScriptJobExecutionMetadata scriptJobExecutionMetadata, Gson gson)
	{
		this.savedScriptRunner = requireNonNull(savedScriptRunner);
		this.scheduledJobTypeFactory = requireNonNull(scheduledJobTypeFactory);
		this.scriptJobExecutionMetadata = requireNonNull(scriptJobExecutionMetadata);
		this.gson = gson;
	}

	/**
	 * The Script JobFactory bean.
	 */
	@Bean
	public JobFactory<ScriptJobExecution> scriptJobFactory()
	{
		return new JobFactory<ScriptJobExecution>()
		{
			@Override
			public Job<ScriptResult> createJob(ScriptJobExecution scriptJobExecution)
			{
				final String name = scriptJobExecution.getName();
				final String parameterString = scriptJobExecution.getParameters();
				return progress ->
				{
					Map<String, Object> params = new HashMap<>();
					params.putAll(gson.fromJson(parameterString, MAP_TOKEN));
					params.put("scriptJobExecutionId", scriptJobExecution.getIdValue());
					ScriptResult scriptResult = savedScriptRunner.runScript(name, params);
					if (scriptResult.getOutputFile() != null)
					{
						scriptJobExecution.setResultUrl(format("/files/%s", scriptResult.getOutputFile().getId()));
					}
					progress.status(format("Script output:%n%s", scriptResult.getOutput()));
					return scriptResult;
				};
			}
		};
	}

	@Lazy
	@Bean
	public ScheduledJobType scriptJobType()
	{
		ScheduledJobType result = scheduledJobTypeFactory.create("script");
		result.setLabel("Script");
		result.setDescription("This job executes a script created in the Scripts plugin.");
		result.setSchema(gson.toJson(of("title", "Script Job", "type", "object", "properties",
				of("name", of("type", "string"), "parameters", of("type", "string")), "required",
				ImmutableList.of("name", "parameters"))));
		result.setJobExecutionType(scriptJobExecutionMetadata);
		return result;
	}
}
