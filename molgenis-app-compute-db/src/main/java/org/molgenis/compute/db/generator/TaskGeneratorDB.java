package org.molgenis.compute.db.generator;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.molgenis.compute.db.WebAppConfig;
import org.molgenis.compute.db.service.RunService;
import org.molgenis.compute.runtime.ComputeBackend;
import org.molgenis.compute5.ComputeCommandLine;
import org.molgenis.compute5.generators.EnvironmentGenerator;
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

	public void generateRun(String parametersFile, String runName, String backendName, Long pollDelay)
			throws DatabaseException, IOException
	{
		LOG.info("Generating task for backend [" + backendName + "] with parametersfile [" + parametersFile + "]");

		Compute compute = ComputeCommandLine.create(parametersFile);
		Database database = WebAppConfig.unathorizedDatabase();

		if (backendName.equalsIgnoreCase("localhost") && (ComputeBackend.findByName(database, "localhost") == null))
		{
			ComputeBackend backend = new ComputeBackend();
			backend.setName("localhost");
			backend.setBackendUrl("localhost");
			backend.setHostType("LOCALHOST");
			backend.setCommand("sh maverick.sh");
			database.add(backend);
		}

		try
		{
			RunService service = new RunService(database, null);

			String environment = new EnvironmentGenerator().getEnvironment(compute);
			service.create(runName, backendName, pollDelay, compute.getTasks(), environment);
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
				.generateRun(
						"/Users/erwin/projects/molgenis/molgenis-compute-core/src/main/resources/workflows/demoNBIC2/parameters.csv",
						"nbic25", "localhost", 2000L);
	}

}
