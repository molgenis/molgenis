package org.molgenis.script;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.molgenis.data.jobs.Job;
import org.molgenis.data.jobs.JobFactory;
import org.molgenis.data.jobs.model.ScheduledJobType;
import org.molgenis.data.jobs.model.ScheduledJobTypeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static java.text.MessageFormat.format;

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

	@Autowired
	public ScheduledScriptConfig(SavedScriptRunner savedScriptRunner, ScheduledJobTypeFactory scheduledJobTypeFactory,
			ScriptJobExecutionMetadata scriptJobExecutionMetadata, Gson gson)
	{
		this.savedScriptRunner = savedScriptRunner;
		this.scheduledJobTypeFactory = scheduledJobTypeFactory;
		this.scriptJobExecutionMetadata = scriptJobExecutionMetadata;
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
						scriptJobExecution.setResultUrl(format("/files/{0}", scriptResult.getOutputFile().getId()));
					}
					progress.appendLog(scriptResult.getOutput());
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
		result.setSchema("{\n\"title\": \"Script Job\",\n\"type\": \"object\",\n\"properties\": {\n"
				+ "\"name\": {\"type\": \"string\"},\n\"parameters\": {\"type\": \"string\"}},\n"
				+ "\"required\": [\"name\", \"parameters\"]\n" + "}");
		result.setJobExecutionType(scriptJobExecutionMetadata);
		return result;
	}
}
