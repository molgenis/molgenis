package org.molgenis.compute.db.executor;

import org.molgenis.compute.runtime.ComputeHost;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 22/08/2012 Time: 14:21
 * To change this template use File | Settings | File Templates.
 */
public interface ComputeExecutor
{
	/**
	 * Execute tasks for a ComputeHost
	 * 
	 * @param computeHost
	 * @param password
	 */
	void executeTasks(ComputeHost computeHost, String password);
}
