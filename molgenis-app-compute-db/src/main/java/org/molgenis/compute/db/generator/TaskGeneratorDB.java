package org.molgenis.compute.db.generator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.molgenis.compute.db.ComputeDbException;
import org.molgenis.compute.db.pilot.PilotService;
import org.molgenis.compute.runtime.ComputeHost;
import org.molgenis.compute.runtime.ComputeParameterValue;
import org.molgenis.compute.runtime.ComputeTask;
import org.molgenis.compute5.ComputeCommandLine;
import org.molgenis.compute5.model.Compute;
import org.molgenis.compute5.model.Task;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.util.ApplicationContextProvider;

/**
 * Created with IntelliJ IDEA. User: georgebyelas Date: 23/04/2013 Time: 08:47
 * To change this template use File | Settings | File Templates.
 */
public class TaskGeneratorDB
{
	private static final Logger LOG = Logger.getLogger(TaskGeneratorDB.class);
	public static final String BACKEND = "backend";

	public void generateTasks(String file, String backendName)
	{
		Compute compute = null;
		// generate here
		try
		{
			System.out.println(new File(".").getCanonicalPath());

			compute = ComputeCommandLine.create(null, new String[]
			{ file }, new File(".").getCanonicalPath());
		}
		catch (IOException e)
		{
			throw new ComputeDbException(e.getMessage());

		}

		List<Task> tasks = compute.getTasks();

		LOG.info("Generating task for [" + backendName + "] with parametersfile [" + file + "]");

		int tasksSize = 0;

		Database db = ApplicationContextProvider.getApplicationContext().getBean("unathorizedDatabase", Database.class);

		try
		{
			db.beginTx();

			List<ComputeHost> computeHosts = db.query(ComputeHost.class).equals(ComputeHost.NAME, backendName).find();

			if (computeHosts.size() > 0)
			{
				ComputeHost computeHost = computeHosts.get(0);

				for (Task task : tasks)
				{
					String name = task.getName();
					String script = task.getScript();

					ComputeTask computeTask = new ComputeTask();
					computeTask.setName(name);
					computeTask.setComputeScript(script);
					computeTask.setComputeHost(computeHost);
					computeTask.setStatusCode(PilotService.TASK_GENERATED);
					computeTask.setInterpreter("bash");

					// find previous tasks in db
					Set<String> prevTaskNames = task.getPreviousTasks();
					List<ComputeTask> previousTasks = new ArrayList<ComputeTask>();
					for (String prevTaskName : prevTaskNames)
					{
						List<ComputeTask> prevTasks = db.query(ComputeTask.class)
								.equals(ComputeTask.NAME, prevTaskName).find();

						if (prevTasks.size() > 0)
						{
							ComputeTask prevTask = prevTasks.get(0);
							previousTasks.add(prevTask);
						}
						else throw new ComputeDbException("No ComputeTask  " + prevTaskName
								+ " is found, when searching for previous task for " + name);

					}

					if (previousTasks.size() > 0) computeTask.setPrevSteps(previousTasks);

					db.add(computeTask);
					tasksSize++;

					tasksSize++;
					LOG.info("Task [" + computeTask.getName() + "] is added\n");

					// add parameter values to DB
					Map<String, Object> tastParameters = task.getParameters();

					for (Map.Entry<String, Object> entry : tastParameters.entrySet())
					{
						String parameterName = entry.getKey();
						String parameterValue = entry.getValue().toString();

						ComputeParameterValue computeParameterValue = new ComputeParameterValue();
						computeParameterValue.setName(parameterName);
						computeParameterValue.setValue(parameterValue);

						ComputeTask taskInDB = db.query(ComputeTask.class)
								.equals(ComputeTask.NAME, computeTask.getName()).find().get(0);

						computeParameterValue.setComputeTask(taskInDB);

						db.add(computeParameterValue);
					}
				}

				LOG.info("Total: " + tasksSize + " is added to database\n");
			}
			else throw new ComputeDbException("ComputeHost does not exist");

			db.commitTx();
		}
		catch (Exception e)
		{
			try
			{
				db.rollbackTx();
			}
			catch (DatabaseException e1)
			{
				e1.printStackTrace();
			}
			throw new ComputeDbException(e.getMessage());
		}
		finally
		{
			IOUtils.closeQuietly(db);
		}

	}

	public static void main(String[] args) throws IOException
	{
		new TaskGeneratorDB()
				.generateTasks(
						"/Users/georgebyelas/Development/molgenis/molgenis-compute-core/src/main/resources/workflows/demoNBIC2/parameters.csv",
						"grid");
	}

}
