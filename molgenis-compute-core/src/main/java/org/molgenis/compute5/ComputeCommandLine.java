package org.molgenis.compute5;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.molgenis.compute5.db.api.*;
import org.molgenis.compute5.generators.CreateWorkflowGenerator;
import org.molgenis.compute5.generators.DocTasksDiagramGenerator;
import org.molgenis.compute5.generators.DocTotalParametersCsvGenerator;
import org.molgenis.compute5.generators.DocWorkflowDiagramGenerator;
import org.molgenis.compute5.generators.EnvironmentGenerator;
import org.molgenis.compute5.generators.TaskGenerator;
import org.molgenis.compute5.generators.local.LocalBackend;
import org.molgenis.compute5.generators.pbs.PbsBackend;
import org.molgenis.compute5.model.Compute;
import org.molgenis.compute5.model.Parameters;
import org.molgenis.compute5.model.Task;
import org.molgenis.compute5.parsers.ParametersCsvParser;
import org.molgenis.compute5.parsers.WorkflowCsvParser;
import org.molgenis.compute5.sysexecutor.SysCommandExecutor;
import org.molgenis.util.tuple.WritableTuple;

import com.google.common.base.Joiner;

/**
 * Commandline program for compute5. Usage: -w workflow.csv -p parameters.csv
 * [-p moreParameters.csv]
 * 
 * NB parameters will be 'natural joined' when overlapping columns.
 */
public class ComputeCommandLine
{
	private static final Logger LOG = Logger.getLogger(ComputeCommandLine.class);

	@SuppressWarnings("static-access")
	public static void main(String[] args) throws Exception
	{
		BasicConfigurator.configure();

		System.out.println("### MOLGENIS COMPUTE ###");
		String version = ComputeCommandLine.class.getPackage().getImplementationVersion();
		if (null == version) version = "development";
		System.out.println("Version: " + version);

		// disable freemarker logging
		freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_NONE);

		// parse options
		ComputeProperties computeProperties = new ComputeProperties(args);

		if (!computeProperties.showHelp)
			new ComputeCommandLine().execute(computeProperties);
	}

	public Compute execute(ComputeProperties computeProperties) throws Exception
	{
		Compute compute = new Compute(computeProperties);

		String userName = null;
		String pass = null;
		ComputeDbApiConnection dbApiConnection = null;
		ComputeDbApiClient dbApiClient = null;

		//check if we are working with database; database default is database=none
		if(!computeProperties.database.equalsIgnoreCase(Parameters.DATABASE_DEFAULT))
		{
			userName = computeProperties.user;
			pass = computeProperties.pass;

			dbApiConnection = new HttpClientComputeDbApiConnection(computeProperties.database,
					computeProperties.port, "/api/v1", userName, pass);
			dbApiClient = new ComputeDbApiClient(dbApiConnection);
		}

		if (computeProperties.create)
		{
			new CreateWorkflowGenerator(computeProperties.createWorkflow);
			return compute;
		}
		else if (computeProperties.clear)
		{
			File file = new File(Parameters.PROPERTIES);

			if(file.delete())
			{
				System.out.println(file.getName() + " is cleared");
			}
			else
			{
				System.out.println("Fail to clear " + file.getName());
			}
			return compute;
		}
		else if (computeProperties.generate)
		{
			System.out.println("Using workflow:         " + new File(computeProperties.workFlow).getAbsolutePath());
			if (defaultsExists(computeProperties)) System.out.println("Using defaults:         "
					+ (new File(computeProperties.defaults)).getAbsolutePath());
			System.out.println("Using parameters:       " + Joiner.on(",").join(computeProperties.parameters));
			System.out.println("Using run (output) dir: " + new File(computeProperties.runDir).getAbsolutePath());
			System.out.println("Using backend:          " + computeProperties.backend);
			System.out.println("Using runID:            " + computeProperties.runId + "\n\n");

			generate(compute, computeProperties);

			if (Parameters.DATABASE_DEFAULT.equals(computeProperties.database))
			{ // if database none (= off), then do following
				if (computeProperties.list)
				{
					// list *.sh files in rundir
					File[] scripts = new File(computeProperties.runDir).listFiles(new FilenameFilter()
					{
						public boolean accept(File dir, String filename)
						{
							return filename.endsWith(".sh");
						}
					});

					System.out.println("Generated jobs that are ready to run:");
					if (null == scripts) System.out.println("None. Remark: the run (output) directory '"
							+ computeProperties.runDir + "' does not exist.");
					else if (0 == scripts.length) System.out.println("None.");
					else for (File script : scripts)
						{
							System.out.println("- " + script.getName());
						}
				}
			}
			else
			{
				String runName = computeProperties.runId;

				String backendName = computeProperties.backend;
				Long pollInterval = Long.parseLong(computeProperties.interval);

				List<Task> tasks = compute.getTasks();
				String environment = compute.getUserEnvironment();

				CreateRunRequest createRunRequest = new CreateRunRequest(runName, backendName, pollInterval, tasks, environment, userName);

				dbApiClient.createRun(createRunRequest);
			}
		}

		if (computeProperties.execute)
		{
			if(computeProperties.database.equalsIgnoreCase(Parameters.DATABASE_DEFAULT))
			{
				String runDir = computeProperties.runDir;
				new SysCommandExecutor().runCommand("sh "+ runDir +"/submit.sh");
				System.out.println("Scripts are executed/submitted on " + computeProperties.backend);
			}
			else
			{
				StartRunRequest startRunRequest = new StartRunRequest(computeProperties.runId, userName, pass);
				dbApiClient.start(startRunRequest);
				System.out.println(computeProperties.runId + "is submitted for execution by user " + userName);
			}
		}
		return compute;
	}

	private static boolean defaultsExists(ComputeProperties computeProperties) throws IOException
	{

		// if exist include defaults.csv in parameterFiles
		if (null == computeProperties.defaults)
		{
			return false;
		}
		else return new File(computeProperties.defaults).exists();
	}

	private static void generate(Compute compute, ComputeProperties computeProperties) throws Exception
	{
		// create a list of parameter files
		List<File> parameterFiles = new ArrayList<File>();

		for (String f : computeProperties.parameters)
			parameterFiles.add(new File(f));
		if (defaultsExists(computeProperties)) parameterFiles.add(new File(computeProperties.defaults));

		// parse param files
		compute.setParameters(ParametersCsvParser.parse(parameterFiles));

		// add command line parameters:
		for (WritableTuple t : compute.getParameters().getValues())
		{
			t.set(Parameters.PATH_COLUMN, computeProperties.path);
			t.set(Parameters.WORKFLOW_COLUMN, computeProperties.workFlow);
			t.set(Parameters.DEFAULTS_COLUMN, computeProperties.defaults);
			t.set(Parameters.PARAMETER_COLUMN, Joiner.on(",").join(computeProperties.parameters));
			t.set(Parameters.RUNDIR_COLUMN, new File(computeProperties.runDir).getAbsolutePath());
			t.set(Parameters.RUNID_COLUMN, computeProperties.runId);
			t.set(Parameters.BACKEND_COLUMN, computeProperties.backend);
			t.set(Parameters.DATABASE_COLUMN, computeProperties.database);
		}

		System.out.println("Starting script generation...");
		// create outputdir
		File dir = new File(computeProperties.runDir);
		computeProperties.runDir = dir.getCanonicalPath();
		dir.mkdirs();

		// document inputs
		new DocTotalParametersCsvGenerator().generate(new File(computeProperties.runDir + "/doc/inputs.csv"),
				compute.getParameters());

		// parse workflow
		compute.setWorkflow(WorkflowCsvParser.parse(computeProperties.workFlow));

		// create environment.txt with user parameters that are used in at least
		// one of the steps
		new EnvironmentGenerator().generate(compute, computeProperties.runDir);

		// generate the tasks
		compute.setTasks(TaskGenerator.generate(compute.getWorkflow(), compute.getParameters(), compute.getComputeProperties()));

		// write the task for the backend
		if (Parameters.BACKEND_PBS.equals(computeProperties.backend))
		{
			new PbsBackend(computeProperties).generate(compute.getTasks(), dir);
		}
		else
		{
			new LocalBackend(computeProperties).generate(compute.getTasks(), dir);
		}

		// generate documentation
		new DocTotalParametersCsvGenerator().generate(new File(computeProperties.runDir + "/doc/outputs.csv"),
				compute.getParameters());
		new DocWorkflowDiagramGenerator().generate(new File(computeProperties.runDir + "/doc"), compute.getWorkflow());
		new DocTasksDiagramGenerator().generate(new File(computeProperties.runDir + "/doc"), compute.getTasks());

		System.out.println("Generation complete.");
	}

}