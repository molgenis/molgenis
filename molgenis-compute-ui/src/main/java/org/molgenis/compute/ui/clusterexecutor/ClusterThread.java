package org.molgenis.compute.ui.clusterexecutor;

import org.molgenis.compute.ui.model.Analysis;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Created with IntelliJ IDEA. User: hvbyelas Date: 5/7/14 Time: 4:08 PM To change this template use File | Settings |
 * File Templates.
 */

public class ClusterThread implements Runnable
{
	public static final String SUBMIT = "submit";
	public static final String CANCEL = "cancel";
	public static final String RERUN = "rerun";

	private final String operation;

	private Analysis analysis = null;
	private ClusterExecutor executor = null;

	private String callbackUri;

	public ClusterThread(ClusterExecutor clusterExecutor, Analysis analysis, String operation)
	{
		this.executor = clusterExecutor;
		this.analysis = analysis;
		this.operation = operation;

		init();
	}

	public void init()
	{
		callbackUri = ServletUriComponentsBuilder.fromCurrentRequest().replacePath("/job").build().toUriString();
	}

	@Override
	public void run()
	{
		if (operation.equalsIgnoreCase(SUBMIT)) executor.submitRun(analysis, callbackUri);
		else if (operation.equalsIgnoreCase(CANCEL)) executor.cancelRun(analysis, callbackUri);
		else if (operation.equalsIgnoreCase(RERUN)) executor.reRun(analysis, callbackUri);

	}
}
