package org.molgenis.script;

import static com.google.common.collect.ImmutableMap.of;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import org.molgenis.jobs.Job;
import org.molgenis.jobs.JobFactory;
import org.molgenis.jobs.model.ScheduledJobType;
import org.molgenis.jobs.model.ScheduledJobTypeFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@SuppressWarnings("SpringJavaAutowiringInspection")
@Configuration
public class ScheduledScriptConfig {
  private static final Type MAP_TOKEN = new TypeToken<Map<String, Object>>() {}.getType();

  private final SavedScriptRunner savedScriptRunner;
  private final ScheduledJobTypeFactory scheduledJobTypeFactory;
  private final ScriptJobExecutionMetadata scriptJobExecutionMetadata;
  private final Gson gson;

  public ScheduledScriptConfig(
      SavedScriptRunner savedScriptRunner,
      ScheduledJobTypeFactory scheduledJobTypeFactory,
      ScriptJobExecutionMetadata scriptJobExecutionMetadata,
      Gson gson) {
    this.savedScriptRunner = requireNonNull(savedScriptRunner);
    this.scheduledJobTypeFactory = requireNonNull(scheduledJobTypeFactory);
    this.scriptJobExecutionMetadata = requireNonNull(scriptJobExecutionMetadata);
    this.gson = gson;
  }

  /** The Script JobFactory bean. */
  @Bean
  public JobFactory<ScriptJobExecution> scriptJobFactory() {
    return new JobFactory<ScriptJobExecution>() {
      @Override
      public Job<ScriptResult> createJob(ScriptJobExecution scriptJobExecution) {
        final String name = scriptJobExecution.getName();
        return progress -> {
          Map<String, Object> params = getParameterMap(scriptJobExecution);
          ScriptResult scriptResult = savedScriptRunner.runScript(name, params);
          if (scriptResult.getOutputFile() != null) {
            scriptJobExecution.setResultUrl(
                format("/files/%s", scriptResult.getOutputFile().getId()));
          }
          if (scriptResult.getOutput() != null) {
            progress.status(format("Script output:%n%s", scriptResult.getOutput()));
          } else {
            progress.status(format("Script has no output."));
          }
          return scriptResult;
        };
      }
    };
  }

  Map<String, Object> getParameterMap(ScriptJobExecution scriptJobExecution) {
    Map<String, Object> params = new HashMap<>();
    String parameterString = scriptJobExecution.getParameters();
    if (!Strings.isNullOrEmpty(parameterString)) {
      params.putAll(gson.fromJson(parameterString, MAP_TOKEN));
    }
    params.put("scriptJobExecutionId", scriptJobExecution.getIdValue());
    return params;
  }

  @Lazy
  @Bean
  public ScheduledJobType scriptJobType() {
    ScheduledJobType result = scheduledJobTypeFactory.create("script");
    result.setLabel("Script");
    result.setDescription("This job executes a script created in the Scripts plugin.");
    result.setSchema(
        gson.toJson(
            of(
                "title",
                "Script Job",
                "type",
                "object",
                "properties",
                of("name", of("type", "string"), "parameters", of("type", "string")),
                "required",
                ImmutableList.of("name"))));
    result.setJobExecutionType(scriptJobExecutionMetadata);
    return result;
  }
}
