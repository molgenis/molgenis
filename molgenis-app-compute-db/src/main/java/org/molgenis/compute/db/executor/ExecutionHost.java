package org.molgenis.compute.db.executor;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.molgenis.util.Ssh;
import org.molgenis.util.SshResult;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 22/08/2012 Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class ExecutionHost extends Ssh
{
	private static final Logger LOG = Logger.getLogger(ExecutionHost.class);

	public ExecutionHost(String host, String user, String password, int port) throws IOException
	{
		super(host, user, password, port);
		LOG.info("... " + host + " is started");
	}

	public void submitPilot(String command) throws IOException
	{
		LOG.info("Executing command [" + command + "] ...");

		SshResult result = executeCommand(command);
		if (!"".equals(result.getStdErr()))
		{
			throw new IOException(result.getStdErr());
		}

		String sOut = result.getStdOut();
		LOG.info("Command StdOut result:\n" + sOut);
	}

}
