package org.molgenis.compute.db.executor;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.db.sysexecutor.SysCommandExecutor;
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
	public static final String BACK_END_GRID = "grid";
	public static final String BACK_END_CLUSTER = "cluster";
	public static final String BACK_END_LOCALHOST = "localhost";
	private static final String DEFAULT_CLUSTER_COMMAND = "qsub /target/gpfs2/gcc/tools/scripts/maverick.sh";
	private static final String DEFAULT_GRID_COMMAND = "glite-wms-job-submit  -d $USER -o pilot-one $HOME/maverick/maverick.jdl";
	private static final String DEFAULT_LOCALHOST_COMMAND = "sh maverick.sh";
	private static final Logger LOG = Logger.getLogger(ComputeExecutorPilotDB.class);

	private ExecutionHost host = null;
	private final Database database;
	private String command;

	@Autowired
	public ComputeExecutorPilotDB(Database database)
	{
		this.database = database;
	}

	@Override
	public void setExecutionHost(ExecutionHost host)
	{
		this.host = host;
	}

	@Override
	public void setCommand(String command)
	{
		this.command = command;
	}

	// actual start pilots here
	@Override
	public void executeTasks(String backend, String backendType)
	{
		// evaluate if we have tasks ready to run on a specific back-end
		int readyToSubmitSize = 0;

		try
		{
			// Clear cache
			database.getEntityManager().clear();

			List<ComputeTask> generatedTasks = database.query(ComputeTask.class)
					.equals(ComputeTask.STATUSCODE, "generated").equals(ComputeTask.BACKENDNAME, backend).find();
			LOG.info("Nr of tasks with status [generated]: [" + generatedTasks.size() + "]");

			readyToSubmitSize = evaluateTasks(generatedTasks);

			List<ComputeTask> readyTasks = database.query(ComputeTask.class).equals(ComputeTask.STATUSCODE, "ready")
					.equals(ComputeTask.BACKENDNAME, backend).find();

			for (ComputeTask task : readyTasks)
			{
				LOG.info("Task ready: [" + task.getName() + "]");
			}

			readyToSubmitSize = readyTasks.size();
		}
		catch (DatabaseException e)
		{
			LOG.error("DatabaseException gettings ready tasks from database", e);
			throw new ComputeDbException(e);
		}

		LOG.info("Nr of tasks ready for execution = " + readyToSubmitSize);

		// start as many pilots as we have tasks ready to run
		for (int i = 0; i < readyToSubmitSize; i++)
		{
			try
			{
				if (backendType.equalsIgnoreCase(BACK_END_GRID))
				{
					String command = this.command == null ? DEFAULT_GRID_COMMAND : this.command;
					host.submitPilot(command);
				}
				else if (backendType.equalsIgnoreCase(BACK_END_CLUSTER))
				{
					String command = this.command == null ? DEFAULT_CLUSTER_COMMAND : this.command;
					host.submitPilot(command);
				}
				else if (backendType.equalsIgnoreCase(BACK_END_LOCALHOST))
				{
					submitPilotLocalhost();
				}
			}
			catch (IOException e)
			{
				LOG.error("Error submit pilot", e);
				throw new ComputeDbException(e);
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

	private void submitPilotLocalhost() throws IOException
	{
		SysCommandExecutor localExecutor = new SysCommandExecutor();

		String command = this.command == null ? DEFAULT_LOCALHOST_COMMAND : this.command;
		LOG.info("Execution command [" + command + "] ...");

		try
		{
			localExecutor.runCommand(command);
		}
		catch (Exception e)
		{
			LOG.error("Exception executing command", e);
		}

		String cmdError = localExecutor.getCommandError();
		String cmdOutput = localExecutor.getCommandOutput();

		LOG.info("Command error output:\n" + cmdError);
		LOG.info("Command output:\n" + cmdOutput);
	}

	private int evaluateTasks(List<ComputeTask> generatedTasks) throws DatabaseException
	{
		int count = 0;
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

		return count;
	}
}
