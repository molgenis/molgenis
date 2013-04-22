package org.molgenis.compute.db;

import java.io.IOException;

import org.molgenis.compute.runtime.ComputeHost;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;

public class InsertComputeHosts
{
	// public static final String DEFAULT_CLUSTER_COMMAND =
	// "qsub /target/gpfs2/gcc/tools/scripts/maverick.sh";

	// public static final String DEFAULT_GRID_COMMAND =
	// "glite-wms-job-submit  -d $USER -o pilot-one $HOME/maverick/maverick.jdl";

	// public static final String DEFAULT_LOCALHOST_COMMAND = "sh maverick.sh";

	/**
	 * @param args
	 * @throws DatabaseException
	 * @throws IOException
	 */
	public static void main(String[] args) throws DatabaseException, IOException
	{
		Database db = new app.JpaDatabase();
		try
		{
			insert(db, "localhost", "LOCALHOST", "localhost", null, 5000, "sh maverick.sh");
			insert(db, "grid", "GRID", "grid", "test", 10000,
					"glite-wms-job-submit  -d $USER -o pilot-one $HOME/maverick/maverick.jdl");
		}
		finally
		{
			db.close();
		}
	}

	private static void insert(Database db, String name, String hostType, String hostName, String userName,
			long pollDelay, String command) throws DatabaseException
	{
		ComputeHost computeHost = new ComputeHost();
		computeHost.setName(name);
		computeHost.setHostType(hostType);
		computeHost.setHostName(hostName);
		computeHost.setUserName(userName);
		computeHost.setPollDelay(pollDelay);
		computeHost.setCommand(command);
		db.add(computeHost);
	}
}
