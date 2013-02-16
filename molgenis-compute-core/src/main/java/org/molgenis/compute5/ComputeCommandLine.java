package org.molgenis.compute5;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.molgenis.compute5.generators.DocTasksDiagramGenerator;
import org.molgenis.compute5.generators.DocTotalParametersCsvGenerator;
import org.molgenis.compute5.generators.DocWorkflowDiagramGenerator;
import org.molgenis.compute5.generators.TaskGenerator;
import org.molgenis.compute5.generators.local.LocalBackend;
import org.molgenis.compute5.model.Compute;
import org.molgenis.compute5.model.Parameters;
import org.molgenis.compute5.model.Task;
import org.molgenis.compute5.parsers.ParametersCsvParser;
import org.molgenis.compute5.parsers.WorkflowCsvParser;
import org.molgenis.util.tuple.WritableTuple;

/**
 * Commandline program for compute5. Usage: -w workflow.csv -p parameters.csv
 * [-p moreParameters.csv]
 * 
 * NB parameters will be 'natural joined' when overlapping columns.
 */
public class ComputeCommandLine
{
	@SuppressWarnings("static-access")
	public static void main(String[] args) throws ParseException, ClassNotFoundException
	{
		// disable freemarker logging
		freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_NONE);

		// setup commandline options
		Options options = new Options();
		Option p = OptionBuilder.withArgName("parameters.csv").isRequired(true).hasArgs().withLongOpt("parameters")
				.withDescription("path to parameter.csv file(s)").create("p");
		Option w = OptionBuilder.withArgName("steps.csv").hasArg().withLongOpt("workflow")
				.withDescription("path to workflow.csv.").create("w");
		Option d = OptionBuilder.withArgName("workdir").hasArg().withLongOpt("workdir")
				.withDescription("path to directory this generates to. Default: currentdir").create("d");
		options.addOption(w);
		options.addOption(p);
		options.addOption(d);

		// parse options
		try
		{
			CommandLineParser parser = new PosixParser();
			CommandLine cmd = parser.parse(options, args);

			String[] parametersCsv = cmd.getOptionValues("p");
			String workflowCsv = "";
			if (null != cmd.getOptionValue("w"))
			{
				workflowCsv = cmd.getOptionValue("w");
			}
			String workDir = "";
			if (null != cmd.getOptionValue("d"))
			{
				workDir = cmd.getOptionValue("d");
			}

			// output scripts + docs
			create(workflowCsv, parametersCsv, workDir);
		}
		catch (Exception e)
		{
			System.out.println(e.getMessage());

			System.out.println("");

			new HelpFormatter().printHelp("compute -p parameters.csv", options);
		}
	}

	public static Compute create(String workflowCsv, String[] parametersCsv, String workDir) throws IOException
	{
		List<File> parameterFiles = new ArrayList<File>();
		for(String f: parametersCsv) parameterFiles.add(new File(f));
		Compute compute = new Compute();
		compute.setParameters(ParametersCsvParser.parse(parameterFiles));
		
		// use workflow or workingdir from parameters?
		if (compute.getParameters().getValues().size() > 0)
		{
			if (!compute.getParameters().getValues().get(0).isNull(Parameters.WORKFLOW_COLUMN))
			{
				workflowCsv = compute.getParameters().getValues().get(0).getString(Parameters.WORKFLOW_COLUMN);
			}
			if (!compute.getParameters().getValues().get(0).isNull(Parameters.WORKDIR_COLUMN))
			{
				workDir = compute.getParameters().getValues().get(0).getString(Parameters.WORKDIR_COLUMN);
			}
		}
		if("".equals(workflowCsv)) throw new IOException("no workflow provided");

		// set constants
		for (WritableTuple t : compute.getParameters().getValues())
		{
			t.set(Parameters.WORKFLOW_COLUMN, new File(workflowCsv).getAbsolutePath());
			t.set(Parameters.WORKDIR_COLUMN, new File(workDir).getAbsolutePath());
		}
		
		System.out.println("### MOLGENIS COMPUTE ###");
		System.out.println("Using workflow:   " + new File(workflowCsv).getAbsolutePath());
		System.out.println("Using parameters: "
				+ parameterFiles);
		System.out.println("Using outputDir:   " + new File(workDir).getAbsolutePath());

		System.out.println(""); // newline

		// create outputdir
		File dir = new File(workDir);
		workDir = dir.getCanonicalPath();
		dir.mkdirs();
		
		// document inputs
		new DocTotalParametersCsvGenerator().generate(new File(workDir + "/doc/inputs.csv"), compute.getParameters());

		// parse workflow
		compute.setWorkflow(WorkflowCsvParser.parse(workflowCsv));


		// generate the tasks
		compute.setTasks(TaskGenerator.generate(compute.getWorkflow(), compute.getParameters()));

		// write the task for the backend
		new LocalBackend().generate(compute.getTasks(), dir);

		// generate outputs folders per task
		for (Task t : compute.getTasks())
		{
			File f = new File(workDir + "/outputs/" + t.getName());
			f.mkdirs();
			System.out.println("Generated "+f.getAbsolutePath());
		}

		// generate documentation
		new DocTotalParametersCsvGenerator().generate(new File(workDir + "/doc/outputs.csv"), compute.getParameters());
		new DocWorkflowDiagramGenerator().generate(new File(workDir + "/doc"), compute.getWorkflow());
		new DocTasksDiagramGenerator().generate(new File(workDir + "/doc"), compute.getTasks());

		System.out.println("Generation complete");
		return compute;
	}
}
