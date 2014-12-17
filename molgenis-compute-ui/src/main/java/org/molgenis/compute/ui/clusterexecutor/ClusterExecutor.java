package org.molgenis.compute.ui.clusterexecutor;

import org.molgenis.compute.ui.model.Analysis;

/**
 * Created by hvbyelas on 12/16/14.
 */
public interface ClusterExecutor
{
	boolean submitRun(Analysis analysis);

	boolean cancelRun(Analysis analysis);
}
