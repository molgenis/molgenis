package org.molgenis.compute.db.pilot;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.compute.runtime.ComputeRun;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.framework.server.MolgenisService;
import org.molgenis.util.ApplicationContextProvider;
import org.molgenis.util.ApplicationUtil;

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
			ScriptBuilder sb = ApplicationContextProvider.getApplicationContext().getBean(ScriptBuilder.class);
			String taskScript = sb.build(task, request.getAppLocation(), request.getServicePath());

			LOG.info("Script for task [" + task.getName() + "] of run [ " + task.getComputeRun().getName() + "]:\n"
					+ taskScript);

			// change status to running
			task.setStatusCode(PilotService.TASK_RUNNING);
			ApplicationUtil.getDatabase().update(task);

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

            List<ComputeBackend> computeBackends = ApplicationUtil.getDatabase().query(ComputeBackend.class)
                .equals(ComputeBackend.NAME, backend).find();

            if(computeBackends.size() > 0)
            {
                ComputeBackend computeBackend = computeBackends.get(0);
                int pilotsStarted = computeBackend.getPilotsStarted();
                computeBackend.setPilotsStarted(pilotsStarted + 1);
                ApplicationUtil.getDatabase().update(computeBackend);
            }
            else
            {
                LOG.error("No backend found for BACKENDNAME [" + backend + "]");
            }

		}
		else
		{
			String logFileContent = FileUtils.readFileToString(request.getFile("log_file"));
			LogFileParser logfile = new LogFileParser(logFileContent);
			String taskName = logfile.getTaskName();
			String runName = logfile.getRunName();
			List<String> logBlocks = logfile.getLogBlocks();
			String runInfo = StringUtils.join(logBlocks, "\n");

			List<ComputeTask> tasks = ApplicationUtil.getDatabase().query(ComputeTask.class).eq(ComputeTask.NAME, taskName)
					.and().eq(ComputeTask.COMPUTERUN_NAME, runName).find();

			if (tasks.isEmpty())
			{
				LOG.warn("No task found for TASKNAME [" + taskName + "] of RUN [" + runName + "]");
				return;
			}

			ComputeTask task = tasks.get(0);

			if ("done".equals(request.getString("status")))
			{
				LOG.info(">>> task [" + taskName + "] of run [" + runName + "] is finished");
				if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING))
				{
					task.setStatusCode(TASK_DONE);
					task.setRunLog(logFileContent);
					task.setRunInfo(runInfo);

					File output = request.getFile("output_file");
					if (output != null)
					{
						task.setOutputEnvironment(FileUtils.readFileToString(output));
					}
				}
				else
				{
					LOG.warn("from done: something is wrong with task [" + taskName + "] of run [" + runName
							+ "] status should be [running] but is [" + task.getStatusCode() + "]");
				}
			}
			else if ("pulse".equals(request.getString("status")))
			{
				if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING))
				{
					LOG.info(">>> pulse from task [" + taskName + "] of run [" + runName + "]");
					task.setRunLog(logFileContent);
					task.setRunInfo(runInfo);
				}
			}
			else if ("nopulse".equals(request.getString("status")))
			{
				if (task.getStatusCode().equalsIgnoreCase(TASK_RUNNING))
				{
					LOG.info(">>> no pulse from task [" + taskName + "] of run [" + runName + "]");
					task.setRunLog(logFileContent);
					task.setRunInfo(runInfo);
					task.setStatusCode("failed");

					File failedLog = request.getFile("failed_log_file");
					if (failedLog != null)
					{
						task.setFailedLog(FileUtils.readFileToString(failedLog));
					}
				}
				else if (task != null && task.getStatusCode().equalsIgnoreCase(TASK_DONE))
				{
					LOG.info("double check: job is finished & no pulse from it for task [" + taskName + "] of run ["
							+ runName + "]");
				}
			}

			ApplicationUtil.getDatabase().update(task);
		}
	}

	private List<ComputeTask> findRunTasksReady(String backendName) throws DatabaseException
	{

//		List<ComputeRun> runs = ApplicationUtil.getDatabase().query(ComputeRun.class)
//				.equals(ComputeRun.COMPUTEBACKEND_NAME, backendName).find();
        List<ComputeRun> runs = ApplicationUtil.getDatabase().query(ComputeRun.class)
				.eq(ComputeRun.COMPUTEBACKEND_NAME, backendName)
                .and().eq(ComputeRun.ISACTIVE, true).find();

        return ApplicationUtil.getDatabase().query(ComputeTask.class)
				.equals(ComputeTask.STATUSCODE, PilotService.TASK_READY).in(ComputeTask.COMPUTERUN, runs).find();
	}
}
