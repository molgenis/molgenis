package org.molgenis.compute.db.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.db.executor.Scheduler;
import org.molgenis.compute.db.generator.TaskGeneratorDB;
import org.molgenis.compute.db.pilot.PilotService;
import org.molgenis.compute.runtime.ComputeHost;
import org.molgenis.compute.runtime.ComputeTask;
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
 * 
 */
@Scope("request")
@Controller
@RequestMapping("/plugin/dashboard")
public class PilotDashboardController
{
	private static final String VIEW_NAME = "PilotDashboard";
	private final Scheduler scheduler;
	private final Database database;
	private final TaskGeneratorDB taskGeneratorDB;

	@Autowired
	public PilotDashboardController(Database database, Scheduler scheduler, TaskGeneratorDB taskGeneratorDB)
	{
		this.scheduler = scheduler;
		this.database = database;
		this.taskGeneratorDB = taskGeneratorDB;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws DatabaseException
	{
		model.addAttribute("hosts", getHostModels());
		return VIEW_NAME;
	}

	@RequestMapping("/start")
	public String start(@RequestParam("id")
	Integer id, @RequestParam("password")
	String password, Model model) throws IOException, DatabaseException
	{
		ComputeHost host = ComputeHost.findById(database, id);
		if (host != null)
		{
			scheduler.schedule(host, password);
		}

		return init(model);
	}

	@RequestMapping("/stop")
	public String stop(@RequestParam("id")
	Integer id, Model model) throws DatabaseException
	{
		scheduler.unschedule(id);
		return init(model);
	}

	@RequestMapping("/generate")
	public String generate(@RequestParam("hostName")
	String hostName, @RequestParam("parametersFile")
	String parametersFile, Model model) throws IOException, DatabaseException
	{
		if (StringUtils.isEmpty(parametersFile))
		{
			model.addAttribute("error", "Please specify a parameters file");
		}
		else
		{
			taskGeneratorDB.generateTasks(parametersFile, hostName);
			model.addAttribute("message", "Tasks generated for " + hostName);
		}

		return init(model);
	}

	@RequestMapping("/regenerate")
	public String regenerateFailedTasks(@RequestParam("hostName")
	String hostName, Model model) throws IOException, DatabaseException
	{
		System.out.println("Resubmit for " + hostName);
		model.addAttribute("message", "Failed tasks regenerated for " + hostName);
		return init(model);
	}

	@RequestMapping(value = "/status", produces = APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseEntity<TaskStatusModel> status(@RequestParam("hostId")
	Integer hostId) throws DatabaseException
	{
		int generated = getTaskStatusCount(hostId, PilotService.TASK_GENERATED);
		int ready = getTaskStatusCount(hostId, PilotService.TASK_READY);
		int running = getTaskStatusCount(hostId, PilotService.TASK_RUNNING);
		int failed = getTaskStatusCount(hostId, PilotService.TASK_FAILED);
		int done = getTaskStatusCount(hostId, PilotService.TASK_DONE);

		TaskStatusModel taskStatusModel = new TaskStatusModel(generated, ready, running, failed, done);
		return new ResponseEntity<TaskStatusModel>(taskStatusModel, HttpStatus.OK);
	}

	private int getTaskStatusCount(Integer hostId, String status) throws DatabaseException
	{
		return database.query(ComputeTask.class).eq(ComputeTask.COMPUTEHOST, hostId).and()
				.eq(ComputeTask.STATUSCODE, status).count();
	}

	@ExceptionHandler(ComputeDbException.class)
	public String showComputeDbException(ComputeDbException e, HttpServletRequest request) throws DatabaseException
	{
		request.setAttribute("hosts", getHostModels());
		request.setAttribute("error", e.getMessage());

		return VIEW_NAME;
	}

	private List<HostModel> getHostModels() throws DatabaseException
	{
		List<HostModel> hostModels = new ArrayList<HostModel>();
		for (ComputeHost computeHost : ComputeHost.find(database))
		{
			HostModel hostModel = new HostModel(computeHost.getId(), computeHost.getName(),
					scheduler.isRunning(computeHost.getId()));
			hostModels.add(hostModel);
		}

		return hostModels;
	}
}
