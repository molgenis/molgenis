package org.molgenis.compute.db.generator;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.WebAppConfig;
import org.molgenis.compute.db.service.RunService;
import org.molgenis.compute5.ComputeCommandLine;
import org.molgenis.compute5.model.Compute;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 23/04/2013 Time: 08:47
 * To change this template use File | Settings | File Templates.
 */
public class TaskGeneratorDB
{
	private static final Logger LOG = Logger.getLogger(TaskGeneratorDB.class);

	public void generateTasks(String parametersFile, String runName, String backendName, Long pollDelay)
			throws DatabaseException, IOException
	{
		LOG.info("Generating task for backend [" + backendName + "] with parametersfile [" + parametersFile + "]");

		Compute compute = ComputeCommandLine.create(parametersFile);
		Database database = WebAppConfig.unathorizedDatabase();
		try
		{
			RunService service = new RunService(database, null);
			service.create(runName, backendName, pollDelay, compute.getTasks());
			LOG.info("Tasks created");
		}
		finally
		{
			database.close();
		}

	}

	public static void main(String[] args) throws IOException, DatabaseException
	{
		new TaskGeneratorDB()
				.generateTasks(
						"/Users/erwin/projects/molgenis/molgenis-compute-core/src/main/resources/workflows/demoNBIC2/parameters.csv",
						"nbic25", "localhost", 2000L);
	}

}
