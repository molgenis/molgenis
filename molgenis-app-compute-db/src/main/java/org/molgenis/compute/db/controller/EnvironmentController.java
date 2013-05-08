package org.molgenis.compute.db.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Serves the user environment and task output environments of a run
 * 
 * 
 * @author erwin
 * 
 */
@Scope("request")
@Controller
@RequestMapping("/environment")
public class EnvironmentController
{
	private final Database database;

	@Autowired
	public EnvironmentController(Database database)
	{
		this.database = database;
	}

	@RequestMapping(value = "/{runName}/user.env", method = RequestMethod.GET)
	public void getUserEnv(@PathVariable("runName")
	String runName, HttpServletResponse response) throws DatabaseException, IOException
	{
		ComputeRun run = ComputeRun.findByName(database, runName);
		if (run != null)
		{
			PrintWriter pw = response.getWriter();
			try
			{
				pw.write(run.getUserEnvironment());
				pw.flush();
			}
			finally
			{
				IOUtils.closeQuietly(pw);
			}
		}
	}

	@RequestMapping(value = "/{runName}/{taskName}.env", method = RequestMethod.GET)
	public void getOutputEnv(@PathVariable("runName")
	String runName, @PathVariable("taskName")
	String taskName, HttpServletResponse response) throws DatabaseException, IOException
	{
		List<ComputeTask> tasks = database.query(ComputeTask.class).eq(ComputeTask.COMPUTERUN_NAME, runName).and()
				.eq(ComputeTask.NAME, taskName).find();

		if (!tasks.isEmpty())
		{
			PrintWriter pw = response.getWriter();
			try
			{
				pw.write(tasks.get(0).getOutputEnvironment());
				pw.flush();
			}
			finally
			{
				IOUtils.closeQuietly(pw);
			}
		}

	}
}
