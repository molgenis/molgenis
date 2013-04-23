package org.molgenis.compute.db.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.db.executor.Scheduler;
import org.molgenis.compute.db.generator.TaskGeneratorDB;
import org.molgenis.compute.runtime.ComputeHost;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

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
		List<HostModel> hostModels = new ArrayList<HostModel>();
		for (ComputeHost computeHost : ComputeHost.find(database))
		{
			HostModel hostModel = new HostModel(computeHost.getId(), computeHost.getName(),
					scheduler.isRunning(computeHost.getId()));
			hostModels.add(hostModel);
		}

		model.addAttribute("hosts", hostModels);

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
			try
			{
				scheduler.schedule(host, password);
			}
			catch (ComputeDbException e)
			{
				model.addAttribute("error", e.getMessage());
			}

		}

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
			try
			{
				taskGeneratorDB.generateTasks(parametersFile, hostName);
				model.addAttribute("message", "Tasks generated for " + hostName);
			}
			catch (ComputeDbException e)
			{
				model.addAttribute("error", e.getMessage());
			}
		}


		this.taskGeneratorDB.generateTasks(parametersFile, hostName);

		return init(model);
	}

	@RequestMapping("/stop")
	public String stop(@RequestParam("id")
	Integer id, String password, Model model) throws DatabaseException
	{
		try
		{
			scheduler.unschedule(id);
		}
		catch (ComputeDbException e)
		{
			model.addAttribute("error", e.getMessage());
		}

		return init(model);
	}

}
