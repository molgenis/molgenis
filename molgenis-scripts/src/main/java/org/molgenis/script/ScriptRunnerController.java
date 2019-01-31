package org.molgenis.script;

import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.rest.util.Href.concatEntityHref;
import static org.springframework.http.MediaType.TEXT_PLAIN_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.core.ui.jobs.JobsController;
import org.molgenis.jobs.JobExecutor;
import org.molgenis.script.core.ScriptException;
import org.molgenis.script.core.UnknownScriptException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Controller for running a script
 *
 * <p>Url: /scripts/${scriptname}/run
 *
 * <p>If the script generates an outputfile that file is streamed to the outputstream else the
 * script output
 */
@Controller
public class ScriptRunnerController {
  private final ScriptJobExecutionFactory scriptJobExecutionFactory;
  private final JobExecutor jobExecutor;
  private final SavedScriptRunner savedScriptRunner;
  private final Gson gson;
  private final JobsController jobsController;

  public ScriptRunnerController(
      ScriptJobExecutionFactory scriptJobExecutionFactory,
      JobExecutor jobExecutor,
      SavedScriptRunner savedScriptRunner,
      Gson gson,
      JobsController jobsController) {
    this.scriptJobExecutionFactory = requireNonNull(scriptJobExecutionFactory);
    this.jobExecutor = requireNonNull(jobExecutor);
    this.savedScriptRunner = requireNonNull(savedScriptRunner);
    this.gson = requireNonNull(gson);
    this.jobsController = requireNonNull(jobsController);
  }

  /**
   * Starts a Script. Will redirect the request to the jobs controller, showing the progress of the
   * started {@link ScriptJobExecution}. The Script's output will be written to the log of the
   * {@link ScriptJobExecution}. If the Script has an outputFile, the URL of that file will be
   * written to the {@link ScriptJobExecution#getResultUrl()}
   *
   * @param scriptName name of the Script to start
   * @param parameters parameter values for the script
   * @throws IOException if an input or output exception occurs when redirecting
   */
  @SuppressWarnings("squid:S3752") // backwards compatability: multiple methods required
  @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/scripts/{name}/start")
  public void startScript(
      @PathVariable("name") String scriptName,
      @RequestParam Map<String, Object> parameters,
      HttpServletResponse response)
      throws IOException {
    String scriptJobExecutionHref = submitScript(scriptName, parameters).getBody();

    response.sendRedirect(jobsController.createJobExecutionViewHref(scriptJobExecutionHref, 1000));
  }

  @SuppressWarnings("squid:S3752") // backwards compatability: multiple methods required
  @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/scripts/{name}/submit", produces = TEXT_PLAIN_VALUE)
  public ResponseEntity<String> submitScript(
      @PathVariable("name") String scriptName, @RequestParam Map<String, Object> parameters) {
    ScriptJobExecution scriptJobExecution = scriptJobExecutionFactory.create();
    scriptJobExecution.setName(scriptName);
    scriptJobExecution.setParameters(gson.toJson(parameters));

    jobExecutor.submit(scriptJobExecution);
    return ok(concatEntityHref(scriptJobExecution));
  }

  /**
   * Runs a Script, waits for it to finish and returns the result.
   *
   * <p>If the result has an outputFile, will redirect to a URL where you can download the result
   * file. Otherwise, if the result has output, will write the script output to the response and
   * serve it as /text/plain.
   *
   * @param scriptName name of the Script to run
   * @param parameters parameter values for the script
   * @param response {@link HttpServletResponse} to return the result
   * @throws IOException if something goes wrong when redirecting or writing the result
   * @throws ScriptException if the script name is unknown or one of the script parameters is
   *     missing
   */
  @SuppressWarnings("squid:S3752") // backwards compatability: multiple methods required
  @RequestMapping(method = {RequestMethod.GET, RequestMethod.POST}, value = "/scripts/{name}/run")
  public void runScript(
      @PathVariable("name") String scriptName,
      @RequestParam Map<String, Object> parameters,
      HttpServletResponse response)
      throws IOException {
    ScriptResult result = savedScriptRunner.runScript(scriptName, parameters);

    if (result.getOutputFile() != null) {
      response.sendRedirect(format("/files/{0}", result.getOutputFile().getId()));
    } else if (StringUtils.isNotBlank(result.getOutput())) {
      response.setContentType("text/plain");

      PrintWriter pw = response.getWriter();
      pw.write(result.getOutput());
      pw.flush();
    }
  }

  @ExceptionHandler(UnknownScriptException.class)
  public void handleUnknownScriptException(UnknownScriptException e, HttpServletResponse response)
      throws IOException {
    response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
  }

  @ExceptionHandler(ScriptException.class)
  public void handleGenerateScriptException(ScriptException e, HttpServletResponse response)
      throws IOException {
    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
  }
}
