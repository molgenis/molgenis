package org.molgenis.compute.db.executor;

import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.util.IOUtils;
import org.molgenis.compute.db.pilot.PilotService;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute.runtime.Pilot;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.auth.MolgenisUser;
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

	public void submitPilot(ComputeBackend computeBackend, String command,
                            String pilotID, String sh, String jdl) throws IOException
	{

        LOG.info("Transferring file maverick" + pilotID + ".jdl ...");

        uploadStringToFile(jdl, "$HOME/maverick/maverick" + pilotID +".jdl");

        LOG.info("Transferring file maverick" + pilotID + ".sh ...");

        uploadStringToFile(sh, "$HOME/maverick/maverick" + pilotID +".sh");

        LOG.info("Executing command [" + command + "] ...");

        boolean success = false;

		while (!success)
        {
            SshResult result = executeCommand(command);
            if (!"".equals(result.getStdErr()))
            {
                //throw new IOException(result.getStdErr());
                System.out.println(result.getStdErr());
            }

            String sOut = result.getStdOut();
            LOG.info("Command StdOut result:\n" + sOut);



            if(sOut.contains(CORRECT_GLITE_RESPOND))
            {
                success = true;
                Database database = null;
                try
                {
                    database = ApplicationUtil.getUnauthorizedPrototypeDatabase();
                    List<ComputeBackend> computeBackends = database.query(ComputeBackend.class)
                            .equals(ComputeBackend.NAME, computeBackend.getName()).find();

                    ComputeBackend backend = null;

                    if(computeBackends.size() > 0)
                    {
                        backend = computeBackends.get(0);
                        int numberOfSubmittedPilots = backend.getPilotsSubmitted();
                        backend.setPilotsSubmitted(numberOfSubmittedPilots + 1);
                        database.update(backend);
                    }
                    else
                    {
                        LOG.error("No backend found for BACKENDNAME [" + computeBackend.getName() + "]");
                    }

                    List<MolgenisUser> owners = database.query(MolgenisUser.class).eq(MolgenisUser.NAME,
                            database.getLogin().getUserName()).find();

                    if(owners.size() == 0)
                        LOG.error("No molgenis user found [" + database.getLogin().getUserName() + "] to submit pilot job");


                    database.beginTx();

                    Pilot pilot = new Pilot();
                    pilot.setValue(pilotID);
                    pilot.setBackend(backend);
                    pilot.setStatus(PilotService.PILOT_SUBMITTED);
                    pilot.setOwner(owners.get(0));

                    database.add(pilot);
                    database.commitTx();

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

        LOG.info("Removing maverick" + pilotID + ".jdl ...");
        executeCommand("rm $HOME/maverick/maverick" + pilotID +".jdl" );

        LOG.info("Removing maverick" + pilotID + ".sh ...");
        executeCommand("rm $HOME/maverick/maverick" + pilotID +".sh" );

    }

}
