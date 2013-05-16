package org.molgenis.compute5;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.BasicConfigurator;
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
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws ParseException, ClassNotFoundException, IOException
	{
		BasicConfigurator.configure();

		System.out.println("### MOLGENIS COMPUTE ###");
		System.out.println("Version: " + ComputeCommandLine.class.getPackage().getImplementationVersion());

		// disable freemarker logging
		freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_NONE);

		// parse options
		ComputeProperties computeProperties = new ComputeProperties(args);

		// output scripts + docs
		try
		{
			create(computeProperties.workFlow, computeProperties.defaults, computeProperties.parameters,
					computeProperties.path, computeProperties.backend, computeProperties.runDir,
					computeProperties.runId, computeProperties.database);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static Compute create(ComputeProperties computeProperties)
	{
		try
		{
			return create(computeProperties.workFlow, computeProperties.defaults, computeProperties.parameters,
					computeProperties.path, computeProperties.backend, computeProperties.runDir,
					computeProperties.runId, computeProperties.database);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return null;
	}

	public static Compute create(String workflowCsv, String defaults, String[] parametersCsv, String path,
			String backend, String runDir, String runID, String database) throws IOException, Exception
	{
		// create a list of parameter files
		List<File> parameterFiles = new ArrayList<File>();

		// if exist include defaults.csv in parameterFiles
		boolean defaultsExists = null != defaults;
		File defaultsFile = File.createTempFile("removeme", null);
		if (defaultsExists)
		{
			defaultsFile = new File(defaults);
			defaultsExists = defaultsFile.exists();
			if (defaultsExists) parameterFiles.add(defaultsFile);
		}

		for (String f : parametersCsv)
			parameterFiles.add(new File(f));

		Compute compute = new Compute();

		// parse param files
		compute.setParameters(ParametersCsvParser.parse(parameterFiles));

		// add command line parameters:
		for (WritableTuple t : compute.getParameters().getValues())
		{
			t.set(Parameters.PATH_COLUMN, path);
			t.set(Parameters.WORKFLOW_COLUMN, workflowCsv);
			t.set(Parameters.DEFAULTS_COLUMN, defaults);
			t.set(Parameters.PARAMETER_COLUMN, Joiner.on(",").join(parametersCsv));
			t.set(Parameters.RUNDIR_COLUMN, new File(runDir).getAbsolutePath());
			t.set(Parameters.RUNID_COLUMN, runID);
			t.set(Parameters.BACKEND_COLUMN, backend);
			t.set(Parameters.DATABASE_COLUMN, database);
		}

		System.out.println("Using workflow:         " + new File(workflowCsv).getAbsolutePath());
		if (defaultsExists) System.out.println("Using defaults:         " + defaultsFile.getAbsolutePath());
		System.out.println("Using parameters:       " + parameterFiles);
		System.out.println("Using run (output) dir: " + new File(runDir).getAbsolutePath());
		System.out.println("Using backend:          " + backend);
		System.out.println("Using runID:            " + runID);

		System.out.println(""); // newline

		// create outputdir
		File dir = new File(runDir);
		runDir = dir.getCanonicalPath();
		dir.mkdirs();

		// document inputs
		new DocTotalParametersCsvGenerator().generate(new File(runDir + "/doc/inputs.csv"), compute.getParameters());

		// parse workflow
		compute.setWorkflow(WorkflowCsvParser.parse(workflowCsv));

		// create environment.txt with user parameters that are used in at least
		// one of the steps
		new EnvironmentGenerator().generate(compute, runDir);

		// generate the tasks
		compute.setTasks(TaskGenerator.generate(compute.getWorkflow(), compute.getParameters()));

		// write the task for the backend
		if (Parameters.BACKEND_PBS.equals(backend))
		{
			new PbsBackend().generate(compute.getTasks(), dir);
		}
		else if (Parameters.BACKEND_LOCAL.equals(backend))
		{
			new LocalBackend().generate(compute.getTasks(), dir);
		}
		else throw new Exception("Unknown backend: " + backend);

		// generate outputs folders per task
		for (Task t : compute.getTasks())
		{
			File f = new File(runDir + "/outputs/" + t.getName());
			f.mkdirs();
			System.out.println("Generated " + f.getAbsolutePath());
		}

		// generate documentation
		new DocTotalParametersCsvGenerator().generate(new File(runDir + "/doc/outputs.csv"), compute.getParameters());
		new DocWorkflowDiagramGenerator().generate(new File(runDir + "/doc"), compute.getWorkflow());
		new DocTasksDiagramGenerator().generate(new File(runDir + "/doc"), compute.getTasks());

		System.out.println("Generation complete");
		return compute;
	}

	/**
	 * Return Compute object, given one single -path to all files
	 * 
	 * @param parametersCsv
	 * @return
	 * @throws IOException
	 */
	public static Compute create(String path) throws IOException, Exception
	{
		ComputeProperties computeProperties = new ComputeProperties(path);

		return ComputeCommandLine.create(computeProperties);
	}
}