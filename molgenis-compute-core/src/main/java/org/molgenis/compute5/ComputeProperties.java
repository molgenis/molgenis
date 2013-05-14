package org.molgenis.compute5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Properties;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.molgenis.compute5.model.Parameters;

import com.google.common.base.Joiner;

public class ComputeProperties
{
	public File propertiesFile;

	public String workFlow = Parameters.WORKFLOW_DEFAULT;
	public String workDir = Parameters.WORKDIR_DEFAULT;
	public String backend = Parameters.BACKEND_DEFAULT;
	public String runDir = Parameters.RUNDIR_DEFAULT;
	public String runId = Parameters.RUNID_DEFAULT;
	public String[] parameters =
	{ Parameters.PARAMETERS_DEFAULT };

	public ComputeProperties(String[] args)
	{
		// set work dir
		parseCommandLine(args);

		createPropertiesFile();

		// parse properties file
		parseProperties();

		// overwrite with command line args
		parseCommandLine(args);

		// save new config
		saveProperties();
	}

	private void createPropertiesFile()
	{
		// get location properties file
		String propFileString = workDir + File.separator + Parameters.PROPERTIES;

		this.propertiesFile = new File(propFileString);

		if (!this.propertiesFile.exists())
		{
			try
			{
				this.propertiesFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	public void parseProperties()
	{
		try
		{
			Properties p = new Properties();
			p.load(new FileInputStream(this.propertiesFile));

			// set this.variables
			this.workFlow = p.getProperty(Parameters.WORKFLOW, this.workFlow);
			this.workDir = p.getProperty(Parameters.WORKDIR, this.workDir);
			this.backend = p.getProperty(Parameters.BACKEND, this.backend);
			this.runDir = p.getProperty(Parameters.RUNDIR, this.runDir);
			this.runId = p.getProperty(Parameters.RUNID, this.runId);

			String parametersCSVString = p.getProperty(Parameters.PARAMETERS);
			if (null != parametersCSVString) this.parameters = parametersCSVString.split("\\s*,\\s*");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void parseCommandLine(String[] args)
	{
		Options options = createOptions();

		CommandLineParser parser = new PosixParser();
		try
		{
			CommandLine cmd = parser.parse(options, args);

			// set this.variables
			this.workFlow = cmd.getOptionValue(Parameters.WORKFLOW_CMNDLINE_OPTION, this.workFlow);
			this.workDir = cmd.getOptionValue(Parameters.WORKDIR_CMNDLINE_OPTION, this.workDir);
			this.backend = cmd.getOptionValue(Parameters.BACKEND_CMNDLINE_OPTION, this.backend);
			this.runDir = cmd.getOptionValue(Parameters.RUNDIR_CMNDLINE_OPTION, this.runDir);
			this.runId = cmd.getOptionValue(Parameters.RUNID_CMNDLINE_OPTION, this.runId);

			String[] cmdParameters = cmd.getOptionValues(Parameters.PARAMETERS_CMNDLINE_OPTION);
			if (null != cmdParameters) this.parameters = cmdParameters;

			// prepend workDir to relative paths
			String wd = this.workDir + (this.workDir.endsWith("/") ? "" : "/");
			if (!this.workFlow.startsWith("/")) this.workFlow = wd + this.workFlow;
			if (!this.runDir.startsWith("/")) this.runDir = wd + this.runDir;
			ArrayList<String> pathParameters = new ArrayList<String>();
			for (String p : this.parameters)
				if (p.startsWith("/")) pathParameters.add(p);
				else pathParameters.add(wd + p);
			this.parameters = pathParameters.toArray(new String[pathParameters.size()]);
		}
		catch (ParseException e)
		{
			System.err.println(e.getMessage() + "\n");
			new HelpFormatter().printHelp("compute -p parameters.csv", options);
		}
	}

	public void saveProperties()
	{
		Properties p = new Properties();
		try
		{
			// set this.variables
			p.setProperty(Parameters.WORKFLOW, this.workFlow);
			p.setProperty(Parameters.WORKDIR, this.workDir);
			p.setProperty(Parameters.BACKEND, this.backend);
			p.setProperty(Parameters.RUNDIR, this.runDir);
			p.setProperty(Parameters.RUNID, this.runId);
			p.setProperty(Parameters.PARAMETERS, Joiner.on(",").join(this.parameters));

			p.store(new FileOutputStream(this.propertiesFile), "This file contains your molgenis-compute properties");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Create Options object specifying all possible command line arguments
	 * 
	 * @return Options
	 */
	@SuppressWarnings("static-access")
	public Options createOptions()
	{
		Options options = new Options();
		Option p = OptionBuilder.withArgName("parameters.csv").isRequired(false).hasArgs().withLongOpt("parameters")
				.withDescription("Path to parameter.csv file(s). Default: parameters.csv").create("p");
		Option w = OptionBuilder.withArgName("workflow.csv").hasArg().withLongOpt(Parameters.WORKFLOW)
				.withDescription("Path to your workflow file. Default: workflow.csv.").create("w");
		Option d = OptionBuilder.hasArg().withLongOpt(Parameters.WORKDIR)
				.withDescription("Path to directory this generates to. Default: <current dir>.")
				.create(Parameters.WORKDIR_CMNDLINE_OPTION);
		Option b = OptionBuilder.hasArg().withLongOpt(Parameters.BACKEND)
				.withDescription("Backend for which you generate. Default: local.")
				.create(Parameters.BACKEND_CMNDLINE_OPTION);
		Option runDir = OptionBuilder.hasArg().withDescription("Directory where jobs are stored")
				.create(Parameters.RUNDIR);
		Option runId = OptionBuilder.hasArg().withLongOpt(Parameters.RUNID)
				.withDescription("Id of the task set which you generate. Default: set01.")
				.create(Parameters.RUNID_CMNDLINE_OPTION);
		options.addOption(p);
		options.addOption(w);
		options.addOption(d);
		options.addOption(b);
		options.addOption(runDir);
		options.addOption(runId);

		return options;
	}
}
