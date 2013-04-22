package org.molgenis.compute.db.executor;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.db.sysexecutor.SysCommandExecutor;
import org.molgenis.compute.runtime.ComputeHost;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 22/08/2012 Time: 14:26
 * To change this template use File | Settings | File Templates.
 */
public class ComputeExecutorPilotDB implements ComputeExecutor
{
	private static final int SSH_PORT = 22;
	private static final Logger LOG = Logger.getLogger(ComputeExecutorPilotDB.class);
	private final Database database;

	@Autowired
	public ComputeExecutorPilotDB(Database database)
	{
		this.database = database;
	}

	@Override
	public void executeTasks(ComputeHost computeHost, String password)
	{
		if (computeHost == null) throw new IllegalArgumentException("ComputeHost is null");

		// Clear cache
		database.getEntityManager().clear();

		ExecutionHost executionHost = null;
		try
		{
			List<ComputeTask> generatedTasks = database.query(ComputeTask.class)
					.equals(ComputeTask.STATUSCODE, "generated").equals(ComputeTask.COMPUTEHOST, computeHost.getId())
					.find();

			LOG.info("Nr of tasks with status [generated]: [" + generatedTasks.size() + "]");

			evaluateTasks(generatedTasks);

			List<ComputeTask> readyTasks = database.query(ComputeTask.class).equals(ComputeTask.STATUSCODE, "ready")
					.equals(ComputeTask.COMPUTEHOST, computeHost.getId()).find();

			for (ComputeTask task : readyTasks)
			{
				LOG.info("Task ready: [" + task.getName() + "]");
			}

			for (int i = 0; i < readyTasks.size(); i++)
			{
				if (computeHost.getHostType().equalsIgnoreCase("localhost"))
				{
					submitPilotLocalhost(computeHost.getCommand());
				}
				else
				{
					LOG.info("Executing command [" + computeHost.getCommand() + "] on host ["
							+ computeHost.getHostName() + "]");

					if (executionHost == null)
					{
						executionHost = new ExecutionHost(computeHost.getHostName(), computeHost.getUserName(),
								password, SSH_PORT);
					}

					executionHost.submitPilot(computeHost.getCommand());
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

	private void evaluateTasks(List<ComputeTask> generatedTasks) throws DatabaseException
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
