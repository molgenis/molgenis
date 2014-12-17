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

import org.molgenis.compute.ui.meta.AnalysisJobMetaData;
import org.molgenis.compute.ui.model.Analysis;
import org.molgenis.compute.ui.model.AnalysisJob;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.runas.RunAsSystem;
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
	private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(ClusterManager.class);

	// FIXME bug remove
	private List<String> idList = new ArrayList<String>();

	@Autowired
	private DataService dataService;

	@Autowired
	private ClusterCurlBuilder builder;

	// FIXME bug removes
	private Analysis run = null;

	// variables, that should come from userUI, DB and config files
	private String password, username, root, url, scheduler;

	@RunAsSystem
	@Override
	public boolean submitRun(Analysis analysis)
	{
		// here read properties, which later will come from username interface (username, password)
		// and DB (clusterRoot)
		readUserProperties();

		LOG.info("SUBMIT Analysis [" + analysis.getName() + "]");
		this.run = analysis;

		String runName = analysis.getName();
		String clusterRoot = root;
		String runDir = clusterRoot + runName;

		boolean prepared = prepareRun(analysis, username, password, runDir);

		if (prepared)
		{
			boolean submitted = submit(analysis, username, password, runDir);
			return submitted;
		}
		else
		{
			LOG.error("Error in preparing ComputeRun");
			return false;
		}
	}

	public boolean prepareRun(Analysis analysis, String username, String password, String runDir)
	{
		LOG.info("Prepare Analysis: " + analysis.getName());

		try
		{
			Thread.sleep(90000);

			JSch jsch = new JSch();

			String user = username;
			String host = url;
			int port = 22;
			String privateKey = ".ssh/id_rsa";

			jsch.addIdentity(privateKey, password);
			LOG.info("identity added ");

			Session session = jsch.getSession(user, host, port);

			LOG.info("session created.");

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
				String builtScript = builder.buildScript(job);
				is = new ByteArrayInputStream(builtScript.getBytes());
				channelSftp.put(is, runDir + "/" + taskName + ".sh");
			}

			channelSftp.exit();
			session.disconnect();

			LOG.info("... run [" + analysis.getName() + "] is prepared");
			return true;
		}
		catch (JSchException e)
		{
			e.printStackTrace();
		}
		catch (SftpException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		return false;
	}

	public boolean submit(Analysis analysis, String username, String password, String runDir)
	{
		try
		{
			Thread.sleep(90000);

			JSch jsch = new JSch();

			String user = username;
			String host = url;
			int port = 22;
			String privateKey = ".ssh/id_rsa";

			jsch.addIdentity(privateKey, password);
			LOG.info("identity added ");

			Session session = jsch.getSession(user, host, port);

			LOG.info("session created.");

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

			LOG.info("submitting ...");

			String command = "cd " + runDir + "; sh submit.sh";
			channelExec.setCommand(command);
			channelExec.connect();

			BufferedReader reader = new BufferedReader(new InputStreamReader(answer));
			String line;
			idList.clear();

			while ((line = reader.readLine()) != null)
			{
				LOG.info(line);
				idList.add(line);
			}

			channelExec.disconnect();
			session.disconnect();

			updateDatabaseWithTaskIDs(idList, analysis);

			LOG.info("Analysis [" + analysis.getName() + "] is submitted");
			return true;
		}
		catch (JSchException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		return false;
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
					// analysisJob.setStatus(JobStatus.SUBMITTED);
					analysisJob.setSchedulerId(Integer.parseInt(submittedID));

					dataService.update(AnalysisJobMetaData.INSTANCE.getName(), analysisJob);
				}

			}
		}
	}

	private AnalysisJob findJob(Analysis analysis, String jobName)
	{
		Iterable<AnalysisJob> jobs = dataService.findAll(AnalysisJobMetaData.INSTANCE.getName(),
				new QueryImpl().eq(AnalysisJobMetaData.ANALYSIS, analysis), AnalysisJob.class);
		for (AnalysisJob job : jobs)
		{
			if (job.getName().equalsIgnoreCase(jobName)) return job;
		}
		return null;
	}

	public boolean cancelRun(Analysis run)
	{
		return false;
	}

	private void readUserProperties()
	{
		Properties prop = new Properties();
		InputStream input = null;

		try
		{
			input = new FileInputStream(".cluster.properties");

			// load a properties file
			prop.load(input);

			password = prop.getProperty(ClusterManager.PASS);
			username = prop.getProperty(ClusterManager.USER);
			root = prop.getProperty(ClusterManager.ROOT);
			url = prop.getProperty(ClusterManager.URL);
			scheduler = prop.getProperty(ClusterManager.SCHEDULER);
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			if (input != null)
			{
				try
				{
					input.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}

}
