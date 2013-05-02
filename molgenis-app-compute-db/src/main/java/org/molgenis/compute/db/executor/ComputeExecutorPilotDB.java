package org.molgenis.compute.db.executor;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.db.WebAppConfig;
import org.molgenis.compute.db.sysexecutor.SysCommandExecutor;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 22/08/2012 Time: 14:26
 * To change this template use File | Settings | File Templates.
 */
public class ComputeExecutorPilotDB implements ComputeExecutor
{
	private static final int SSH_PORT = 22;
	private static final Logger LOG = Logger.getLogger(ComputeExecutorPilotDB.class);

	@Override
	public void executeTasks(ComputeRun computeRun, String username, String password)
	{
		if (computeRun == null) throw new IllegalArgumentException("ComputRun is null");

		Database database = null;

		ExecutionHost executionHost = null;
		try
		{
			database = WebAppConfig.unathorizedDatabase();

			List<ComputeTask> generatedTasks = database.query(ComputeTask.class)
					.equals(ComputeTask.STATUSCODE, "generated").equals(ComputeTask.COMPUTERUN, computeRun).find();

			LOG.info("Nr of tasks with status [generated]: [" + generatedTasks.size() + "]");

			evaluateTasks(database, generatedTasks);

			List<ComputeTask> readyTasks = database.query(ComputeTask.class).equals(ComputeTask.STATUSCODE, "ready")
					.equals(ComputeTask.COMPUTERUN, computeRun).find();

			for (ComputeTask task : readyTasks)
			{
				LOG.info("Task ready: [" + task.getName() + "]");
			}

			for (int i = 0; i < readyTasks.size(); i++)
			{
				if (computeRun.getHostType().equalsIgnoreCase("localhost"))
				{
					submitPilotLocalhost(computeRun.getCommand());
				}
				else
				{
					LOG.info("Executing command [" + computeRun.getCommand() + "] on backend ["
							+ computeRun.getBackendUrl() + "]");

					if (executionHost == null)
					{
						executionHost = new ExecutionHost(computeRun.getBackendUrl(), username, password, SSH_PORT);
					}

					executionHost.submitPilot(computeRun.getCommand());
				}

				// sleep, because we have a strange behavior in pilot service
				try
				{
					Thread.sleep(2000);
				}
				catch (InterruptedException e)
				{
					LOG.error("Interrupted exception while sleeping", e);
				}
			}

		}
		catch (DatabaseException e)
		{
			LOG.error("DatabaseException executing tasks", e);
			throw new ComputeDbException("DatabaseException executing tasks", e);
		}
		catch (IOException e)
		{
			LOG.error("IOException executing tasks", e);
			throw new ComputeDbException("DatabaseException executing tasks", e);
		}
		finally
		{
			if (executionHost != null)
			{
				executionHost.close();
			}

			IOUtils.closeQuietly(database);
		}
	}

	private void submitPilotLocalhost(String command)
	{
		LOG.info("Execution command [" + command + "] ...");

		SysCommandExecutor localExecutor = new SysCommandExecutor();
		try
		{
			localExecutor.runCommand(command);
		}
		catch (Exception e)
		{
			LOG.error("Exception executing command [" + command + "] on localhost", e);
			throw new ComputeDbException("Exception executing command [" + command + "] on localhost", e);
		}

		String cmdError = localExecutor.getCommandError();
		String cmdOutput = localExecutor.getCommandOutput();

		LOG.info("Command error output:\n" + cmdError);
		LOG.info("Command output:\n" + cmdOutput);
	}

	private void evaluateTasks(Database database, List<ComputeTask> generatedTasks) throws DatabaseException
	{
		for (ComputeTask task : generatedTasks)
		{
			boolean isReady = true;
			List<ComputeTask> prevSteps = task.getPrevSteps();
			for (ComputeTask prev : prevSteps)
			{
				if (!prev.getStatusCode().equalsIgnoreCase("done")) isReady = false;
			}

			if (isReady)
			{
				LOG.info(">>> TASK [" + task.getName() + "] is ready for execution");

				task.setStatusCode("ready");
				database.update(task);
			}
		}

	}

}
