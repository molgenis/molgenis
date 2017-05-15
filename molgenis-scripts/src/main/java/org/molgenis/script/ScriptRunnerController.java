package org.molgenis.script;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.jobs.JobExecutor;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.ui.jobs.JobsController;
import org.molgenis.ui.menu.MenuReaderService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.util.Map;

import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;
import static org.molgenis.data.support.Href.concatEntityHref;

/**
 * Controller for running a script
 * <p>
 * Url: /scripts/${scriptname}/run
 * <p>
 * If the script generates an outputfile that file is streamed to the outputstream else the script output
 */
@Controller
public class ScriptRunnerController
{
	private final ScriptJobExecutionFactory scriptJobExecutionFactory;
	private final JobExecutor jobExecutor;
	private final SavedScriptRunner savedScriptRunner;
	private final Gson gson;
	private final MenuReaderService menuReaderService;
	private final DataService dataService;
	private final UserAccountService userAccountService;

	private ScriptRunnerController(ScriptJobExecutionFactory scriptJobExecutionFactory, JobExecutor jobExecutor,
			SavedScriptRunner savedScriptRunner, Gson gson, MenuReaderService menuReaderService,
			DataService dataService, UserAccountService userAccountService)
	{
		this.scriptJobExecutionFactory = requireNonNull(scriptJobExecutionFactory);
		this.jobExecutor = requireNonNull(jobExecutor);
		this.savedScriptRunner = requireNonNull(savedScriptRunner);
		this.gson = requireNonNull(gson);
		this.menuReaderService = requireNonNull(menuReaderService);
		this.dataService = requireNonNull(dataService);
		this.userAccountService = requireNonNull(userAccountService);
	}

	@RequestMapping("/scripts/{name}/start")
	public void startScript(@PathVariable("name") String scriptName, @RequestParam Map<String, Object> parameters,
			HttpServletResponse response) throws IOException, URISyntaxException
	{
		ScriptJobExecution scriptJobExecution = scriptJobExecutionFactory.create();
		scriptJobExecution.setName(scriptName);
		scriptJobExecution.setParameters(gson.toJson(parameters));
		scriptJobExecution.setUser(userAccountService.getCurrentUser().getUsername());

		jobExecutor.submit(scriptJobExecution);

		String jobHref = concatEntityHref("/api/v2", scriptJobExecution.getEntityType().getId(),
				scriptJobExecution.getIdValue());
		String jobControllerURL = menuReaderService.getMenu().findMenuItemPath(JobsController.ID);
		response.sendRedirect(format("{0}/viewJob/?jobHref={1}&refreshTimeoutMillis=1000", jobControllerURL, jobHref));
	}

	@RequestMapping("/scripts/{name}/run")
	public void runScript(@PathVariable("name") String scriptName, @RequestParam Map<String, Object> parameters,
			HttpServletResponse response) throws IOException
	{
		ScriptResult result = savedScriptRunner.runScript(scriptName, parameters);

		if (result.getOutputFile() != null)
		{
			response.sendRedirect(format("/files/{0}", result.getOutputFile().getId()));
		}
		else if (StringUtils.isNotBlank(result.getOutput()))
		{
			response.setContentType("text/plain");

			PrintWriter pw = response.getWriter();
			pw.write(result.getOutput());
			pw.flush();
		}
	}

	@ExceptionHandler(ScriptException.class)
	public void handleGenerateScriptException(ScriptException e, HttpServletResponse response) throws IOException
	{
		response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
	}
}
