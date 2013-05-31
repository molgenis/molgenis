package org.molgenis.compute.db.executor;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.util.ApplicationUtil;
import org.molgenis.util.Ssh;
import org.molgenis.util.SshResult;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 22/08/2012 Time: 15:39
 * To change this template use File | Settings | File Templates.
 */
public class ExecutionHost extends Ssh
{

    public static final String CORRECT_GLITE_RESPOND = "glite-wms-job-submit Success";
	private static final Logger LOG = Logger.getLogger(ExecutionHost.class);

	public ExecutionHost(String host, String user, String password, int port) throws IOException
	{
		super(host, user, password, port);
		LOG.info("... " + host + " is started");
	}

	public void submitPilot(ComputeBackend computeBackend, String command) throws IOException
	{
		LOG.info("Executing command [" + command + "] ...");

		SshResult result = executeCommand(command);
		if (!"".equals(result.getStdErr()))
		{
			throw new IOException(result.getStdErr());
		}

		String sOut = result.getStdOut();
		LOG.info("Command StdOut result:\n" + sOut);

        if(sOut.contains(CORRECT_GLITE_RESPOND))
        {
            Database database = null;
            try
            {
                database = ApplicationUtil.getUnauthorizedPrototypeDatabase();
                List<ComputeBackend> computeBackends = database.query(ComputeBackend.class)
                        .equals(ComputeBackend.NAME, computeBackend.getName()).find();

                if(computeBackends.size() > 0)
                {
                    ComputeBackend backend = computeBackends.get(0);
                    int numberOfSubmittedPilots = backend.getPilotsSubmitted();
                    backend.setPilotsSubmitted(numberOfSubmittedPilots + 1);
                    database.update(backend);
                }
                else
                {
                    LOG.error("No backend found for BACKENDNAME [" + computeBackend.getName() + "]");
                }
            }
            catch (DatabaseException e)
            {
               LOG.error("No backend found for BACKENDNAME [" + computeBackend.getName() + "]");
               e.printStackTrace();
            }
            finally
            {
                IOUtils.closeQuietly(database);
            }
        }

    }

}
