package org.molgenis.compute.ui.clusterexecutor;

import org.molgenis.compute.ui.model.Analysis;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by hvbyelas on 10/15/14.
 */
public class ClusterManager
{

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

	public void reRunJobs(Analysis analysis)
	{
		(new Thread(new ClusterThread(clusterExecutor, analysis, ClusterThread.RERUN))).start();
	}

}
