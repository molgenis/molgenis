package org.molgenis.compute.db.executor;

import org.molgenis.compute.runtime.ComputeRun;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 22/08/2012 Time: 14:21
 * To change this template use File | Settings | File Templates.
 */
public interface ComputeExecutor
{
	/**
	 * Execute tasks for a ComputeRun
	 * 
	 * @param computeRun
	 * @param username
	 * @param password
	 */
	void executeTasks(ComputeRun computeRun, String username, String password);
}
