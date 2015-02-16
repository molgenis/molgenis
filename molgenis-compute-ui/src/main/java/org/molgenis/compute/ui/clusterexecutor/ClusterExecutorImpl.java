package org.molgenis.compute.ui.clusterexecutor;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.molgenis.auth.MolgenisUser;
import org.molgenis.compute.ui.meta.AnalysisJobMetaData;
import org.molgenis.compute.ui.meta.AnalysisMetaData;
import org.molgenis.compute.ui.meta.MolgenisUserKeyMetaData;
import org.molgenis.compute.ui.model.Analysis;
import org.molgenis.compute.ui.model.AnalysisJob;
import org.molgenis.compute.ui.model.AnalysisStatus;
import org.molgenis.compute.ui.model.JobStatus;
import org.molgenis.compute.ui.model.MolgenisUserKey;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.runas.RunAsSystem;
import org.molgenis.security.token.MolgenisToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

/**
 * Created by hvbyelas on 10/14/14.
 */
public class ClusterExecutorImpl implements ClusterExecutor
{
	private static final Logger LOG = LoggerFactory.getLogger(ClusterExecutorImpl.class);

	private static final String SLURM_CANCEL = "scancel ";
	private static final String PBS_CANCEL = "qdel ";

	public static final String SLURM = "slurm";
	public static final String PBS = "pbs";

	@Autowired
	private DataService dataService;

	@Autowired
	private ClusterCurlBuilder builder;

	@RunAsSystem
	@Override
	public boolean submitRun(Analysis analysis, String callbackUri)
	{
		String clusterRoot = analysis.getBackend().getWorkDir();
		String runDir = clusterRoot + analysis.getName();
		// here read properties, which later will come from username interface (username, password)
		// and DB (clusterRoot)

		LOG.info("SUBMIT Analysis [" + analysis.getName() + "]");

		// get SSH key pair for current user
		MolgenisUserKey userKeyPair = dataService.findOne(MolgenisUserKeyMetaData.INSTANCE.getName(),
				new QueryImpl().eq(MolgenisUserKeyMetaData.USER, analysis.getUser()), MolgenisUserKey.class);

		// submit analysis through SSH channel
		MolgenisUserSecureChannel userSecureChannel = null;
		try
		{
			userSecureChannel = new MolgenisUserSecureChannel(userKeyPair);

			Session session = null;
			try
			{
				session = userSecureChannel.getSession(analysis.getBackend().getHost(), 22);

				session.connect();
				LOG.info("session connected.....");

				// copy files to backend
				prepareAnalysis(analysis, session, runDir, callbackUri);

			}
			finally
			{
				if (session != null)
				{
					session.disconnect();
					LOG.info("session disconnected.....");
				}
			}

			LOG.debug("sleeping 90 seconds ...");
			Thread.sleep(90000);
			LOG.debug("finished sleeping 90 seconds");

			try
			{
				session = userSecureChannel.getSession(analysis.getBackend().getHost(), 22);

				session.connect();
				LOG.info("session connected.....");

				// execute submit script on backend
				submitAnalysis(analysis, session, runDir);

				analysis.setWasRun(true);

				dataService.update(AnalysisMetaData.INSTANCE.getName(), analysis);
			}
			finally
			{
				if (session != null)
				{
					session.disconnect();
					LOG.info("session disconnected.....");
				}
			}

		}
		catch (IOException | JSchException | InterruptedException | SftpException e)
		{
			LOG.error("Failed to submit analysis", e);
			throw new RuntimeException(e);
		}
		finally
		{
			if (userSecureChannel != null)
			{
				try
				{
					userSecureChannel.close();
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}

		return true;
	}

	private void prepareAnalysis(Analysis analysis, Session session, String runDir, String callbackUri)
			throws JSchException, InterruptedException, SftpException
	{
		LOG.info("Prepare Analysis: " + analysis.getName());

		MolgenisUser molgenisUser = dataService.findOne(MolgenisUser.ENTITY_NAME,
				new QueryImpl().eq(MolgenisUser.USERNAME,
						analysis.getUser()), MolgenisUser.class);

		MolgenisToken token = dataService.findOne(MolgenisToken.ENTITY_NAME,
				new QueryImpl().eq(MolgenisToken.MOLGENISUSER,
						molgenisUser), MolgenisToken.class);

		Channel channel = session.openChannel("sftp");
		channel.setInputStream(System.in);
		channel.setOutputStream(System.out);
		channel.connect();
		LOG.info("shell channel connected....");

		ChannelSftp channelSftp = (ChannelSftp) channel;
		ChannelExec channelExec = (ChannelExec) session.openChannel("exec");

		LOG.info("create run directory...");

		channelExec.setCommand("mkdir " + runDir);
		channelExec.connect();
		channelExec.disconnect();

		// give some time to create directory
		TimeUnit.SECONDS.sleep(1);

		LOG.info("scripts transferring...");
		InputStream is = new ByteArrayInputStream(analysis.getSubmitScript().getBytes());
		channelSftp.put(is, runDir + "/submit.sh");

		Iterable<AnalysisJob> jobs = dataService.findAll(AnalysisJobMetaData.INSTANCE.getName(),
				new QueryImpl().eq(AnalysisJobMetaData.ANALYSIS, analysis), AnalysisJob.class);

		for (AnalysisJob job : jobs)
		{
			String taskName = job.getName();
			String builtScript = builder.buildScript(token, job, callbackUri);
			is = new ByteArrayInputStream(builtScript.getBytes());
			channelSftp.put(is, runDir + "/" + taskName + ".sh");
		}

		channelSftp.exit();
		session.disconnect();

		LOG.info("... run [" + analysis.getName() + "] is prepared");
	}

	private void submitAnalysis(Analysis analysis, Session session, String runDir) throws JSchException, IOException
	{
		Channel channel = session.openChannel("sftp");
		channel.setInputStream(System.in);
		channel.setOutputStream(System.out);
		channel.connect();
		LOG.info("shell channel connected....");

		ChannelExec channelExec = (ChannelExec) session.openChannel("exec");

		InputStream answer = channelExec.getInputStream();

		LOG.info("submitting ...");

		String command = "cd " + runDir + "; sh submit.sh";
		channelExec.setCommand(command);
		channelExec.connect();

		BufferedReader reader = new BufferedReader(new InputStreamReader(answer));
		String line;

		List<String> idList = new ArrayList<String>();
		while ((line = reader.readLine()) != null)
		{
			LOG.info(line);
			idList.add(line);
		}

		channelExec.disconnect();
		session.disconnect();

		updateDatabaseWithTaskIDs(idList, analysis);

		LOG.info("Analysis [" + analysis.getName() + "] is submitted");
	}

	private void updateDatabaseWithTaskIDs(List<String> idList, Analysis analysis)
	{
		for (String str : idList)
		{
			int index = str.indexOf(":");
			if (index > 0)
			{
				String jobName = str.substring(0, index);
				String submittedID = str.substring(index + 1);

				AnalysisJob analysisJob = findJob(analysis, jobName);
				if (analysisJob != null)
				{
					analysisJob.setStatus(JobStatus.SCHEDULED);
					analysisJob.setSchedulerId(submittedID);

					dataService.update(AnalysisJobMetaData.INSTANCE.getName(), analysisJob);
				}

			}
		}
	}

	private AnalysisJob findJob(Analysis analysis, String jobName)
	{
		Iterable<AnalysisJob> jobs = dataService.findAll(AnalysisJobMetaData.INSTANCE.getName(),
				new QueryImpl().eq(AnalysisJobMetaData.ANALYSIS, analysis), AnalysisJob.class);
		// TODO: put it into query
		for (AnalysisJob job : jobs)
		{
			if (job.getName().equalsIgnoreCase(jobName)) return job;
		}
		return null;
	}

	public boolean cancelRun(Analysis analysis, String callbackUri)
	{
		LOG.info("Canceling Analysis [" + analysis.getName() + "]");

		MolgenisUserSecureChannel userSecureChannel = null;

		try
		{
			Thread.sleep(90000);

			MolgenisUserKey userKeyPair = dataService.findOne(MolgenisUserKeyMetaData.INSTANCE.getName(),
					new QueryImpl().eq(MolgenisUserKeyMetaData.USER, analysis.getUser()), MolgenisUserKey.class);

			// submit analysis through SSH channel
			userSecureChannel = new MolgenisUserSecureChannel(userKeyPair);

			Session session = userSecureChannel.getSession(analysis.getBackend().getHost(), 22);
			session.connect();
			LOG.info("session connected.....");

			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);

			session.connect();
			LOG.info("session connected.....");

			Channel channel = session.openChannel("sftp");
			channel.setInputStream(System.in);
			channel.setOutputStream(System.out);
			channel.connect();
			LOG.info("shell channel connected....");

			ChannelExec channelExec = (ChannelExec) session.openChannel("exec");

			InputStream answer = channelExec.getInputStream();

			LOG.info("cancelling jobs ...");

			Iterable<AnalysisJob> jobs = dataService.findAll(AnalysisJobMetaData.INSTANCE.getName(),
					new QueryImpl().eq(AnalysisJobMetaData.ANALYSIS, analysis), AnalysisJob.class);

			String schedulerType = analysis.getBackend().getSchedulerType().toString();

			boolean anyJobCancelled = false;
			for (AnalysisJob job : jobs)
			{
				if (job.getStatus() == JobStatus.RUNNING)
				{
					anyJobCancelled = true;
					String command = "";
					if (schedulerType.equalsIgnoreCase(SLURM)) command = SLURM_CANCEL + job.getSchedulerId();
					else if (schedulerType.equalsIgnoreCase(PBS)) command = PBS_CANCEL + job.getSchedulerId();
					else LOG.error("Unsupported scheduler type [" + schedulerType + "]");

					LOG.info(command);
					channelExec.setCommand(command);
					channelExec.connect();

					BufferedReader reader = new BufferedReader(new InputStreamReader(answer));
					String line;

					while ((line = reader.readLine()) != null)
					{
						LOG.info(line);
					}

					job.setStatus(JobStatus.CANCELLED);
					dataService.update(AnalysisJobMetaData.INSTANCE.getName(), job);
				}
			}

			channelExec.disconnect();
			session.disconnect();

			if (anyJobCancelled)
			{
				analysis.setStatus(AnalysisStatus.CANCELLED);
				dataService.update(AnalysisMetaData.INSTANCE.getName(), analysis);
			}

			LOG.info("Analysis [" + analysis.getName() + "] is cancelled");
			return true;
		}
		catch (IOException | JSchException | InterruptedException e)
			{
				LOG.error("Failed to submit analysis", e);
				throw new RuntimeException(e);
			}
			finally
			{
				if (userSecureChannel != null)
				{
					try
					{
						userSecureChannel.close();
					}
					catch (IOException e)
					{
						throw new RuntimeException(e);
					}
				}
			}
	}

	public boolean reRun(Analysis analysis, String callbackUri)
	{
		String clusterRoot = analysis.getBackend().getWorkDir();
		String runDir = clusterRoot + analysis.getName();
		// here read properties, which later will come from username interface (username, password)
		// and DB (clusterRoot)

		LOG.info("SUBMIT Analysis [" + analysis.getName() + "]");

		// get SSH key pair for current user
		MolgenisUserKey userKeyPair = dataService.findOne(MolgenisUserKeyMetaData.INSTANCE.getName(),
				new QueryImpl().eq(MolgenisUserKeyMetaData.USER, analysis.getUser()), MolgenisUserKey.class);

		// submit analysis through SSH channel
		MolgenisUserSecureChannel userSecureChannel = null;
		try
		{
			userSecureChannel = new MolgenisUserSecureChannel(userKeyPair);

			Session session = null;
			try
			{
				session = userSecureChannel.getSession(analysis.getBackend().getHost(), 22);

				session.connect();
				LOG.info("session connected.....");

				submitAnalysis(analysis, session, runDir);

				analysis.setWasRun(true);
				dataService.update(AnalysisMetaData.INSTANCE.getName(), analysis);
			}
			finally
			{
				if (session != null)
				{
					session.disconnect();
					LOG.info("session disconnected.....");
				}
			}

		}
		catch (IOException | JSchException  e)
		{
			LOG.error("Failed to submit analysis", e);
			throw new RuntimeException(e);
		}
		finally
		{
			if (userSecureChannel != null)
			{
				try
				{
					userSecureChannel.close();
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}

		return true;
	}

}
