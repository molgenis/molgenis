package org.molgenis.compute.db.service;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.db.executor.Scheduler;
import org.molgenis.compute.db.model.RunStatus;
import org.molgenis.compute.db.pilot.PilotService;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
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
	private static final String DEFAULT_GRID_COMMAND = "glite-wms-job-submit  -d $USER -o pilot-one $HOME/maverick/maverick.jdl";
	private static final long DEFAULT_POLL_DELAY = 10000;
	private final Database database;
	private final Scheduler scheduler;

	@Autowired
	public RunService(Database database, Scheduler scheduler)
	{
		this.database = database;
		this.scheduler = scheduler;
	}

	/*
	 * TODO public void create(ComputeRun run, List<ComputeTask> tasks) throws
	 * DatabaseException { if (ComputeRun.findByName(database, run.getName()) !=
	 * null) { throw new ComputeDbException("Run name [" + run.getName() +
	 * "] already exists"); }
	 * 
	 * if (StringUtils.isEmpty(run.getCommand())) {
	 * run.setCommand(DEFAULT_GRID_COMMAND); }
	 * 
	 * if ((run.getPollDelay() == null) || (run.getPollDelay() == 0)) {
	 * run.setPollDelay(DEFAULT_POLL_DELAY); } else if (run.getPollDelay() <
	 * 2000) { throw new ComputeDbException("PollDelay must be 2000 minimum"); }
	 * 
	 * database.add(run);
	 * 
	 * Map<String, List<String>> taskPrevStepNames = new HashMap<String,
	 * List<String>>(); for (ComputeTask task : tasks) { List<String>
	 * prevStepNames = new ArrayList<String>(); for (ComputeTask prevStep :
	 * task.getPrevSteps()) { prevStepNames.add(prevStep.getName()); }
	 * taskPrevStepNames.put(task.getName(), prevStepNames);
	 * 
	 * task.setComputeRun(run); task.setPrevSteps(Collections.<ComputeTask>
	 * emptyList()); }
	 * 
	 * 
	 * }
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

	private int getTaskStatusCount(Integer runId, String status) throws DatabaseException
	{
		return database.query(ComputeTask.class).eq(ComputeTask.COMPUTERUN, runId).and()
				.eq(ComputeTask.STATUSCODE, status).count();
	}
}
