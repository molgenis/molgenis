package org.molgenis.compute.db.executor;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 22/08/2012 Time: 14:21
 * To change this template use File | Settings | File Templates.
 */
public interface ComputeExecutor
{
	void executeTasks(String backend, String backendType);

	void setExecutionHost(ExecutionHost host);

	/**
	 * The command to execute on the host
	 * 
	 * @param command
	 */
	public void setCommand(String command);
}
