package org.molgenis.script;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.JobFactory;
import org.molgenis.data.jobs.model.JobType;
import org.molgenis.data.jobs.model.JobTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static java.text.MessageFormat.format;

@Configuration
public class ScheduledScriptConfig
{
	private static final Type MAP_TOKEN = new TypeToken<Map<String, Object>>()
	{
	}.getType();

	@Autowired
	SavedScriptRunner savedScriptRunner;

	@Autowired
	JobTypeFactory jobTypeFactory;

	@Autowired
	ScriptJobExecutionMetadata scriptJobExecutionMetadata;

	@Autowired
	Gson gson;

	/**
	 * The Script JobFactory bean.
	 */
	@Bean
	public JobFactory scriptJobFactory()
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
						scriptJobExecution.setResultUrl(format("/files/{0}", scriptResult.getOutputFile().getId()));
					}
					progress.appendLog(scriptResult.getOutput());
					return scriptResult;
				};
			}

			@Override
			public JobType getJobType()
			{
				JobType result = jobTypeFactory.create("script");
				result.setLabel("Script");
				result.setDescription("This job executes a script created in the Scripts plugin.");
				result.setSchema("TODO");
				result.setJobExecutionType(scriptJobExecutionMetadata);
				return result;
			}
		};
	}
}
