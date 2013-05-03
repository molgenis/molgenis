package org.molgenis.compute.db.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.db.executor.Scheduler;
import org.molgenis.compute.db.model.RunStatus;
import org.molgenis.compute.db.pilot.PilotService;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.compute.runtime.ComputeParameterValue;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute.runtime.ComputeTaskHistory;
import org.molgenis.compute5.model.Task;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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

	public ComputeRun create(String name, String backendName, Long pollDelay, List<Task> tasks)
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
				throw new ComputeDbException("Run with name [" + backendName + "] already exists");
			}

			database.beginTx();

			ComputeRun run = new ComputeRun();
			run.setComputeBackend(backend);
			run.setName(name);
			run.setPollDelay(pollDelay == null ? DEFAULT_POLL_DELAY : pollDelay);
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

	public void resubmitFailedTasks(String runName)
	{
		LOG.info("Resubmit failed tasks for run [" + runName + "]");
		try
		{
			List<ComputeTask> failedTasks = database.query(ComputeTask.class)
					.equals(ComputeTask.STATUSCODE, PilotService.TASK_FAILED).and()
					.equals(ComputeTask.COMPUTERUN_NAME, runName).find();

			if (failedTasks.isEmpty())
			{
				return;
			}

			database.beginTx();

			for (ComputeTask task : failedTasks)
			{
				ComputeTaskHistory history = new ComputeTaskHistory();
				history.setComputeTask(task);
				history.setRunLog(task.getRunLog());
				history.setStatusTime(new Date());
				history.setStatusCode(task.getStatusCode());
				database.add(history);

				// mark job as generated
				task.setStatusCode("generated");
				task.setRunLog("");

				LOG.info("Task [" + task.getName() + "] changes from failed to generated");
			}

			database.commitTx();
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

	private int getTaskStatusCount(Integer runId, String status) throws DatabaseException
	{
		return database.query(ComputeTask.class).eq(ComputeTask.COMPUTERUN, runId).and()
				.eq(ComputeTask.STATUSCODE, status).count();
	}
}
