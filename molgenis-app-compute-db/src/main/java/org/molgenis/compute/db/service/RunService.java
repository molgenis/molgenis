package org.molgenis.compute.db.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.db.executor.Scheduler;
import org.molgenis.compute.db.pilot.PilotService;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.compute.runtime.ComputeParameterValue;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute5.db.api.RunStatus;
import org.molgenis.compute5.model.Task;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * ComputeRun service facade
 * 
 * @author erwin
 * 
 */
@Scope("request")
@Component
public class RunService
{
	private static final Logger LOG = Logger.getLogger(RunService.class);
	private static final long DEFAULT_POLL_DELAY = 30000;
	private final Database database;
	private final Scheduler scheduler;

	@Autowired
	public RunService(Database database, Scheduler scheduler)
	{
		this.database = database;
		this.scheduler = scheduler;
	}

	/**
	 * Create a new ComputeRun
	 * 
	 * @param name
	 * @param backendName
	 * @param pollDelay
	 * @param tasks
	 * @param environment
	 * @return the new ComputeRun
	 */
	public ComputeRun create(String name, String backendName, Long pollDelay, List<Task> tasks, String userEnvironment)
	{
		try
		{
			ComputeBackend backend = ComputeBackend.findByName(database, backendName);
			if (backend == null)
			{
				throw new ComputeDbException("Unknown backend with name [" + backendName + "]");
			}

			if (ComputeRun.findByName(database, name) != null)
			{
				throw new ComputeDbException("Run with name [" + name + "] already exists");
			}

			database.beginTx();

			ComputeRun run = new ComputeRun();
			run.setComputeBackend(backend);
			run.setName(name);
			run.setPollDelay(pollDelay == null ? DEFAULT_POLL_DELAY : pollDelay);
			run.setUserEnvironment(userEnvironment);
			database.add(run);

			// Add tasks to db
			for (Task task : tasks)
			{
				ComputeTask computeTask = new ComputeTask();
				computeTask.setName(task.getName());
				computeTask.setComputeRun(run);
				computeTask.setInterpreter("bash");
				computeTask.setStatusCode(PilotService.TASK_GENERATED);
				computeTask.setComputeScript(task.getScript());
				database.add(computeTask);

				if (LOG.isDebugEnabled())
				{
					LOG.debug("Added task [" + task.getName() + "]");
				}
			}

			// Set prev tasks
			for (Task task : tasks)
			{
				ComputeTask computeTask = ComputeTask.findByNameComputeRun(database, task.getName(), run.getId());

				List<ComputeTask> prevTasks = new ArrayList<ComputeTask>();
				for (String prevTaskName : task.getPreviousTasks())
				{
					ComputeTask prevTask = ComputeTask.findByNameComputeRun(database, prevTaskName, run.getId());
					if (prevTask == null)
					{
						throw new ComputeDbException("Previous task [" + prevTaskName + "]  not found");
					}

					prevTasks.add(prevTask);
				}

				if (!prevTasks.isEmpty())
				{
					computeTask.setPrevSteps(prevTasks);
				}

				if (LOG.isDebugEnabled())
				{
					LOG.debug("Set prevSteps for [" + task.getName() + "]");
				}
			}

			// Add parameters
			for (Task task : tasks)
			{
				ComputeTask computeTask = ComputeTask.findByNameComputeRun(database, task.getName(), run.getId());

				for (Map.Entry<String, Object> param : task.getParameters().entrySet())
				{
					ComputeParameterValue computeParameterValue = new ComputeParameterValue();
					computeParameterValue.setComputeTask(computeTask);
					computeParameterValue.setName(param.getKey());
					if (param.getValue() != null)
					{
						computeParameterValue.setValue(param.getValue().toString());
					}

					database.add(computeParameterValue);

					if (LOG.isDebugEnabled())
					{
						LOG.debug("Added parameter [" + param.getKey() + "]");
					}
				}
			}

			database.commitTx();

			LOG.info("Create new run [" + name + "] done");

			return run;
		}
		catch (DatabaseException e)
		{
			try
			{
				database.rollbackTx();
			}
			catch (DatabaseException e1)
			{
				LOG.error("Exception rollback transaction create ComputeRun", e1);
			}

			String msg = "DatabaseException starting creating ComputeRun with name [" + name + "]";
			LOG.error(msg, e);
			throw new ComputeDbException(msg, e);
		}

	}

	/**
	 * Start pilots
	 * 
	 * @param runName
	 * @param username
	 * @param password
	 */
	public void start(String runName, String username, String password)
	{
		try
		{
			ComputeRun run = ComputeRun.findByName(database, runName);
			if (run == null)
			{
				throw new ComputeDbException("Unknown run name [" + runName + "]");
			}

			scheduler.schedule(run, username, password);
		}
		catch (DatabaseException e)
		{
			String msg = "DatabaseException starting run with name [" + runName + "]";
			LOG.error(msg, e);
			throw new ComputeDbException(msg, e);
		}
	}

	/**
	 * Stop database olling
	 * 
	 * @param runName
	 */
	public void stop(String runName)
	{
		try
		{
			ComputeRun run = ComputeRun.findByName(database, runName);
			if (run == null)
			{
				throw new ComputeDbException("Unknown run name [" + runName + "]");
			}

			scheduler.unschedule(run.getId());
		}
		catch (DatabaseException e)
		{
			String msg = "DatabaseException stopping run with name [" + runName + "]";
			LOG.error(msg, e);
			throw new ComputeDbException(msg, e);
		}
	}

	/**
	 * Check is a run is currently running
	 * 
	 * @param runName
	 * @return
	 */
	public boolean isRunning(String runName)
	{
		try
		{
			ComputeRun run = ComputeRun.findByName(database, runName);
			if (run == null)
			{
				throw new ComputeDbException("Unknown run name [" + runName + "]");
			}

			return scheduler.isRunning(run.getId());
		}
		catch (DatabaseException e)
		{
			String msg = "DatabaseException check running for run  [" + runName + "]";
			LOG.error(msg, e);
			throw new ComputeDbException(msg, e);
		}
	}

	/**
	 * Get the status of all tasks of a run
	 * 
	 * @param runName
	 * @return
	 */
	public RunStatus getStatus(String runName)
	{
		try
		{
			ComputeRun run = ComputeRun.findByName(database, runName);
			if (run == null)
			{
				throw new ComputeDbException("Unknown run name [" + runName + "]");
			}

			int generated = getTaskStatusCount(run.getId(), PilotService.TASK_GENERATED);
			int ready = getTaskStatusCount(run.getId(), PilotService.TASK_READY);
			int running = getTaskStatusCount(run.getId(), PilotService.TASK_RUNNING);
			int failed = getTaskStatusCount(run.getId(), PilotService.TASK_FAILED);
			int done = getTaskStatusCount(run.getId(), PilotService.TASK_DONE);

			return new RunStatus(generated, ready, running, failed, done);
		}
		catch (DatabaseException e)
		{
			String msg = "DatabaseException getting status for run  [" + runName + "]";
			LOG.error(msg, e);
			throw new ComputeDbException(msg, e);
		}
	}

	/**
	 * Resubmit all failed tasks of a run
	 * 
	 * @param runName
	 * @return the number of resubmitted failed tasks
	 */
	public int resubmitFailedTasks(String runName)
	{
		LOG.info("Resubmit failed tasks for run [" + runName + "]");
		try
		{
			List<ComputeTask> tasks = database.query(ComputeTask.class)
					.equals(ComputeTask.STATUSCODE, PilotService.TASK_FAILED).and()
					.equals(ComputeTask.COMPUTERUN_NAME, runName).find();

			if (tasks.isEmpty())
			{
				return 0;
			}

			for (ComputeTask task : tasks)
			{
				// mark job as generated
				// entry to history is added by ComputeTaskDecorator
				task.setStatusCode("generated");
				task.setRunLog(null);
				task.setRunInfo(null);

				LOG.info("Task [" + task.getName() + "] changed from failed to generated");
			}

			database.update(tasks);

			return tasks.size();
		}
		catch (DatabaseException e)
		{
			try
			{
				database.rollbackTx();
			}
			catch (DatabaseException e1)
			{
				LOG.error("Exception rollback transaction resubmitFailedTasks", e1);
			}

			String msg = "DatabaseException resubmitting failed tasks for run  [" + runName + "]";
			LOG.error(msg, e);
			throw new ComputeDbException(msg, e);
		}
	}

	/**
	 * Remove a run from the dashboard (not from the database)
	 * 
	 * @param runName
	 */
	public void removeFromDashboard(String runName)
	{
		try
		{
			ComputeRun run = ComputeRun.findByName(database, runName);
			if (run == null)
			{
				throw new ComputeDbException("Unknown run name [" + runName + "]");
			}

			run.setShowInDashboard(false);
			database.update(run);
		}
		catch (DatabaseException e)
		{
			String msg = "DatabaseException removing  [" + runName + "] from dashboard";
			LOG.error(msg, e);
			throw new ComputeDbException(msg, e);
		}
	}

	private int getTaskStatusCount(Integer runId, String status) throws DatabaseException
	{
		return database.query(ComputeTask.class).eq(ComputeTask.COMPUTERUN, runId).and()
				.eq(ComputeTask.STATUSCODE, status).count();
	}
}
