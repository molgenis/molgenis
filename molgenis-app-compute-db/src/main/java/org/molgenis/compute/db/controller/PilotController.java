package org.molgenis.compute.db.controller;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.molgenis.compute.db.executor.ComputeExecutorTask;
import org.molgenis.compute.runtime.ComputeHost;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Start and stop pilots
 * 
 * @author erwin
 * 
 */
@Scope("request")
@Controller
@RequestMapping("/plugin/pilot")
public class PilotController
{
	private final ComputeExecutorTask computeExecutorTask;
	private final Database database;

	@Autowired
	public PilotController(Database database, ComputeExecutorTask computeExecutorTask)
	{
		if (computeExecutorTask == null) throw new IllegalArgumentException("ComputeExecutorTask is null");
		this.computeExecutorTask = computeExecutorTask;
		this.database = database;
	}

	@RequestMapping(method = RequestMethod.GET)
	public String init(Model model) throws DatabaseException
	{
		model.addAttribute("pilotForm", new PilotForm(computeExecutorTask));
		model.addAttribute("computeHosts", ComputeHost.find(database));

		return "pilot";
	}

	@RequestMapping("/start")
	public String start(PilotForm pilotForm, Model model) throws IOException, DatabaseException
	{
		if (!computeExecutorTask.isRunning())
		{
			ComputeHost computeHost = ComputeHost.findById(database, pilotForm.getComputeHostId());
			if (computeHost != null)
			{
				if (!computeHost.getHostType().equalsIgnoreCase("localhost")
						&& StringUtils.isEmpty(pilotForm.getPassword()))
				{
					model.addAttribute("errorMessage", "Please supply a password");
				}
				else
				{
					try
					{
						computeExecutorTask.start(computeHost, pilotForm.getPassword());
					}
					catch (Exception e)
					{
						model.addAttribute("errorMessage", e.getMessage());
					}
				}
			}
		}

		return init(model);
	}

	@RequestMapping("/stop")
	public String stop(Model model) throws DatabaseException
	{
		if (computeExecutorTask.isRunning())
		{
			computeExecutorTask.stop();
		}

		return init(model);
	}
}
