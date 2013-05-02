package org.molgenis.compute.db.pilot;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.framework.server.MolgenisService;
import org.molgenis.util.WebAppUtil;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date:
 * /usr/local/mysql/bin/mysql -u molgenis -pmolgenis compute < $HOME/compute.sql
 * 20/07/2012 Time: 16:53 To change this template use File | Settings | File
 * Templates.
 */
public class PilotService implements MolgenisService
{
	private static final Logger LOG = Logger.getLogger(PilotService.class);

	public static final String TASK_GENERATED = "generated";
	public static final String TASK_READY = "ready";
	public static final String TASK_RUNNING = "running";
	public static final String TASK_FAILED = "failed";
	public static final String TASK_DONE = "done";

	public PilotService(MolgenisContext mc)
	{
	}

	@Override
	public synchronized void handleRequest(MolgenisRequest request, MolgenisResponse response) throws ParseException,
			DatabaseException, IOException
	{
		LOG.debug(">> In handleRequest!");
		LOG.debug(request);

		if ("started".equals(request.getString("status")))
		{
			String backend = request.getString("backend");

			LOG.info("Looking for task to execute for host [" + backend + "]");

			List<ComputeTask> tasks = findRunTasksReady(backend);

			if (tasks.isEmpty())
			{
				LOG.info("No tasks to start for host [" + backend + "]");
				return;
			}

			ComputeTask task = tasks.get(0);

			// we add task id to the run listing to identify task when
			// it is done
			String pilotServiceUrl = request.getAppLocation() + request.getServicePath();

			String taskScript = String.format(
					"echo TASKID:%s\n%s\ncp log.log done.log\ncurl -F status=done -F log_file=@done.log %s\n",
					task.getName(), task.getComputeScript().replaceAll("\r", ""), pilotServiceUrl);

			LOG.info("Script for task [" + task.getName() + "] :\n" + taskScript);

			// change status to running
			task.setStatusCode(PilotService.TASK_RUNNING);
			WebAppUtil.getDatabase().update(task);

			// send response
			PrintWriter pw = response.getResponse().getWriter();
			try
			{
				pw.write(taskScript);
				pw.flush();
			}
			finally
			{
				IOUtils.closeQuietly(pw);
			}
		}
		else
		{
			String logFileContent = FileUtils.readFileToString(request.getFile("log_file"));
			LogFileParser logfile = new LogFileParser(logFileContent);
			String taskID = logfile.getTaskID();
			List<String> logBlocks = logfile.getLogBlocks();

			logBlocks.add(0, "Task: " + taskID);
			String runInfo = StringUtils.join(logBlocks, "\n");

			List<ComputeTask> tasks = WebAppUtil.getDatabase().query(ComputeTask.class).eq(ComputeTask.NAME, taskID)
					.find();

			if (tasks.isEmpty())
			{
				LOG.warn("No task found for TASKID [" + taskID + "]");
				return;
			}

			ComputeTask task = tasks.get(0);

			if ("done".equals(request.getString("status")))
			{
				LOG.info(">>> task [" + taskID + "] is finished");
				if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING))
				{
					task.setStatusCode(TASK_DONE);
					task.setRunLog(logFileContent);
					task.setRunInfo(runInfo);
				}
				else
				{
					LOG.warn("from done: something is wrong with [" + taskID + "] status should be [running] but is ["
							+ task.getStatusCode() + "]");
				}
			}
			else if ("pulse".equals(request.getString("status")))
			{
				if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING))
				{
					LOG.info(">>> pulse from " + taskID);
					task.setRunLog(logFileContent);
					task.setRunInfo(runInfo);
				}
			}
			else if ("nopulse".equals(request.getString("status")))
			{
				if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING))
				{
					LOG.info(">>> no pulse from " + taskID);
					task.setRunLog(logFileContent);
					task.setRunInfo(runInfo);
					task.setStatusCode("failed");
				}
				else if (task != null && task.getStatusCode().equalsIgnoreCase(TASK_DONE))
				{
					LOG.info("double check: job is finished & no pulse from it");
				}
			}

			WebAppUtil.getDatabase().update(task);
		}
	}

	private List<ComputeTask> findRunTasksReady(String backendUrl) throws DatabaseException
	{
		List<ComputeRun> runs = WebAppUtil.getDatabase().query(ComputeRun.class)
				.equals(ComputeRun.BACKENDURL, backendUrl).find();

		return WebAppUtil.getDatabase().query(ComputeTask.class)
				.equals(ComputeTask.STATUSCODE, PilotService.TASK_READY).in(ComputeTask.COMPUTERUN, runs).find();
	}
}
