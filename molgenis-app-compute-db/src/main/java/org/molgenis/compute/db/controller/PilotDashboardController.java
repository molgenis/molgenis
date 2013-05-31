package org.molgenis.compute.db.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.db.service.RunService;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute5.db.api.RunStatus;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Start and stop pilots, show status
 * 
 * @author erwin
 */
@Scope("request")
@Controller
@RequestMapping(PilotDashboardController.URI)
public class PilotDashboardController
{
	public static final String URI = "/plugin/dashboard";
	private static final String VIEW_NAME = "PilotDashboard";
	private final Database database;
	private final RunService runService;

	@Autowired
	public PilotDashboardController(Database database, RunService runService)
	{
		this.database = database;
		this.runService = runService;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws DatabaseException
	{
		model.addAttribute("runs", getRunModels());
		return VIEW_NAME;
	}

	@RequestMapping("/start")
	public String start(@RequestParam("run")
	String runName, @RequestParam("username")
	String username, @RequestParam("password")
	String password, Model model) throws IOException, DatabaseException
	{
		runService.start(runName, username, password);
		return init(model);
	}

	@RequestMapping("/stop")
	public String stop(@RequestParam("run")
	String runName, Model model) throws DatabaseException
	{
		runService.stop(runName);
		return init(model);
	}

	@RequestMapping("/close")
	public String close(@RequestParam("run")
	String runName, Model model) throws DatabaseException
	{
		runService.removeFromDashboard(runName);
		return init(model);
	}

    @RequestMapping("/activate")
    public String activate(@RequestParam("run")
    String runName, Model model) throws DatabaseException
    {
        runService.activate(runName);
        return init(model);
    }

    @RequestMapping("/inactivate")
    public String inactivate(@RequestParam("run")
    String runName, Model model) throws DatabaseException
    {
        runService.inactivate(runName);
        return init(model);
    }

    @RequestMapping("/resubmit")
	public String resubmitFailedTasks(@RequestParam("run")
	String runName, Model model) throws DatabaseException
	{
		int count = runService.resubmitFailedTasks(runName);
		model.addAttribute("message", "Resubmitted " + count + " failed tasks for '" + runName + "'");
		return init(model);
	}

	@RequestMapping(value = "/status", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<RunStatus> status(@RequestParam("run")
	String runName) throws DatabaseException
	{
		RunStatus status = runService.getStatus(runName);
		return new ResponseEntity<RunStatus>(status, HttpStatus.OK);
	}

	@ExceptionHandler(ComputeDbException.class)
	public String showComputeDbException(ComputeDbException e, HttpServletRequest request) throws DatabaseException
	{
		request.setAttribute("runs", getRunModels());
		request.setAttribute("error", e.getMessage());

		return VIEW_NAME;
	}

	private List<RunModel> getRunModels() throws DatabaseException
	{
		List<RunModel> runModels = new ArrayList<RunModel>();

		Query<ComputeRun> runs = database.query(ComputeRun.class).eq(ComputeRun.SHOWINDASHBOARD, true)
				.sortDESC("creationTime");
		for (ComputeRun run : runs.find())
		{
			runModels.add(new RunModel(run.getName(),
                    runService.isRunning(run.getName()),
                    runService.isSubmitting(run.getName()),
                    runService.isComplete(run.getName()),
                    run.getComputeBackend()
					.getBackendUrl(), run.getCreationTime()));
		}

		return runModels;
	}
}
