package org.molgenis.compute.db.pilot;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.framework.server.MolgenisService;
import org.molgenis.util.WebAppUtil;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date:
 * /usr/local/mysql/bin/mysql -u molgenis -pmolgenis compute < $HOME/compute.sql
 * 20/07/2012 Time: 16:53 To change this template use File | Settings | File
 * Templates.
 */
public class PilotService implements MolgenisService
{
	private static final Logger LOG = Logger.getLogger(PilotService.class);

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
			LOG.info(">>> looking for a task to execute at " + backend);

			// curl call back statement in the end of the analysis script
			// pulse calls are present in the pilot job, final call back in
			// actual analysis script

			String pilotServiceUrl = request.getAppLocation() + request.getServicePath();

			List<ComputeTask> tasks = WebAppUtil.getDatabase().query(ComputeTask.class)
					.equals(ComputeTask.STATUSCODE, "ready").equals(ComputeTask.BACKENDNAME, backend).find();

			if (tasks.size() > 0)
			{
				ComputeTask task = tasks.get(0);

				if (task != null)
				{
					String taskName = task.getName();
					String taskScript = task.getComputeScript();
					taskScript = taskScript.replaceAll("\r", "");

					// we add task id to the run listing to identify task when
					// it is done
					String curlDone = "\ncp log.log done.log\ncurl -F status=done -F log_file=@done.log "
							+ pilotServiceUrl + "\n";

					taskScript = "echo TASKID:" + taskName + "\n" + taskScript + curlDone;
					LOG.info("Script for task [" + taskName + "] :\n" + taskScript);

					// change status to running
					task.setStatusCode("running");
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
				if (task.getStatusCode().equalsIgnoreCase("running"))
				{
					task.setStatusCode("done");
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
				if (task.getStatusCode().equalsIgnoreCase("running"))
				{
					LOG.info(">>> pulse from " + taskID);
					task.setRunLog(logFileContent);
                    task.setRunInfo(runInfo);
				}
			}
			else if ("nopulse".equals(request.getString("status")))
			{
				if (task.getStatusCode().equalsIgnoreCase("running"))
				{
					LOG.info(">>> no pulse from " + taskID);
					task.setRunLog(logFileContent);
                    task.setRunInfo(runInfo);
					task.setStatusCode("failed");
				}
				else if (task != null && task.getStatusCode().equalsIgnoreCase("done"))
				{
					LOG.info("double check: job is finished & no pulse from it");
				}
			}

			WebAppUtil.getDatabase().update(task);
		}
	}
}
