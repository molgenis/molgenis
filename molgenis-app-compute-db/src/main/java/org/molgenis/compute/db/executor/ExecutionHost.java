package org.molgenis.compute.db.executor;

import org.molgenis.util.Ssh;
import org.molgenis.util.SshResult;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 22/08/2012 Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class ExecutionHost extends Ssh
{

	public ExecutionHost(String host, String user, String password, int port) throws IOException
	{
		super(host, user, password, port);
		System.out.println("... " + host + " is started");
	}

    public void submitPilotGrid() throws IOException
	{
		// do wee need any unique id here? - if yes why?
		String uniqueID = "pilot-one";

		String command = "glite-wms-job-submit  -d $USER -o " + uniqueID + " $HOME/maverick/maverick.jdl";
		System.out.println(">>> " + command);
		SshResult result = this.executeCommand(command);

		if (!"".equals(result.getStdErr()))
		{
			throw new IOException(result.getStdErr());
		}

		String sOut = result.getStdOut();
		System.out.println(sOut);
	}

	public void submitPilotCluster() throws IOException
	{
		String command = "qsub /target/gpfs2/gcc/tools/scripts/maverick.sh";
		System.out.println(">>> " + command);

		SshResult result = this.executeCommand(command);

		if (!"".equals(result.getStdErr()))
		{
			throw new IOException(result.getStdErr());
		}

		String sOut = result.getStdOut();
		System.out.println(sOut);
	}
}
