package org.molgenis.compute.db;

import org.molgenis.compute.db.executor.ComputeExecutor;
import org.molgenis.compute.db.executor.ComputeExecutorPilotDB;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 13/09/2012 Time: 16:10
 * To change this template use File | Settings | File Templates.
 */
public class RunPilotsOnBackEnd
{
	public static void main(String[] args)
	{
		if (args.length != 4)
		{
			System.out.println("please specify backend, user, password and backend type, which can be grid or cluster");
			System.exit(1);
		}

		String host = args[0];
		String user = args[1];
		String pass = args[2];
		String backendType = args[3];

		System.out.println("execute with pilots on " + host);
		// execute generated tasks with pilots
		ComputeExecutor executor = new ComputeExecutorPilotDB();
		executor.startHostWithCredentials(host, user, pass, 22);

        while (true)
        {
            executor.executeTasks(host, backendType);
            try
            {
                Thread.sleep(30000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
