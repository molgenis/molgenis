package org.molgenis.compute.db.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.db.model.RunStatus;
import org.molgenis.compute.db.service.RunService;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
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
@RequestMapping("/plugin/dashboard")
public class PilotDashboardController
{
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

	/*
	 * @RequestMapping("/regenerate") public String
	 * regenerateFailedTasks(@RequestParam("hostName") String hostName, Model
	 * model) throws IOException, DatabaseException {
	 * System.out.println("Resubmit for " + hostName);
	 * 
	 * List<ComputeTask> tasks = database.query(ComputeTask.class)
	 * .equals(ComputeTask.STATUSCODE, PilotService.TASK_FAILED)
	 * .equals(ComputeTask.COMPUTEHOST_NAME, hostName).find();
	 * 
	 * database.beginTx(); for (ComputeTask task : tasks) { ComputeTaskHistory
	 * history = new ComputeTaskHistory(); history.setComputeTask(task);
	 * history.setRunLog(task.getRunLog()); Date date = new Date();
	 * history.setStatusTime(date); history.setStatusCode(task.getStatusCode());
	 * database.add(history);
	 * 
	 * // mark job as generated task.setStatusCode("generated");
	 * task.setRunLog(""); System.out.println(task.getName() +
	 * " >>> changed from failed to generated"); } database.commitTx();
	 * model.addAttribute("message", tasks.size() +
	 * " Failed tasks resubmitted for " + hostName); return init(model); }
	 */

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
		for (ComputeRun run : ComputeRun.find(database))
		{
			runModels.add(new RunModel(run.getName(),  runService.isRunning(run.getName()), run.getBackendUrl()));
		}

		return runModels;
	}
}
