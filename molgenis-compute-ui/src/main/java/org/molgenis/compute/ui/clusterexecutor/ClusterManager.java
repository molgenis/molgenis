package org.molgenis.compute.ui.clusterexecutor;

import org.molgenis.compute.ui.model.Analysis;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by hvbyelas on 10/15/14.
 */
public class ClusterManager
{
	public static final String PASS = "pass";
	public static final String USER = "user";
	public static final String ROOT = "root";
	public static final String URL = "url";
	public static final String API_USER = "api_user";
	public static final String API_PASS = "api_pass";
	public static final String SERVER_IP = "server_ip";
	public static final String SERVER_PORT = "server_port";
	public static final String SCHEDULER = "scheduler";
	public static final String BACKEND = "backend";

	@Autowired
	private ClusterExecutor clusterExecutor;

	public void executeAnalysis(Analysis analysis)
	{

		(new Thread(new ClusterThread(clusterExecutor, analysis, ClusterThread.SUBMIT))).start();
	}

	public void cancelRunJobs(Analysis analysis)
	{
			(new Thread(new ClusterThread(clusterExecutor, analysis, ClusterThread.CANCEL))).start();

	}
}
