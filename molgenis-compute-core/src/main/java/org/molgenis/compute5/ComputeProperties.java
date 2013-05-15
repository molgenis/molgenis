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

	public String path = Parameters.PATH_DEFAULT;
	public String workFlow = Parameters.WORKFLOW_DEFAULT;
	public String defaults = Parameters.DEFAULTS_DEFAULT;
	public String[] parameters =
	{ Parameters.PARAMETERS_DEFAULT };
	public String backend = Parameters.BACKEND_DEFAULT;
	public String runDir = Parameters.RUNDIR_DEFAULT;
	public String runId = Parameters.RUNID_DEFAULT;
	public String database = Parameters.DATABASE_DEFAULT;

	public ComputeProperties(String[] args)
	{
		// set path
		setPath(args);

		// prepend path to defaults
		updateDefaults(path);

		createPropertiesFile();

		// parse properties file
		parseProperties();

		// overwrite with command line args
		parseCommandLine(args);

		// save new config
		saveProperties();
	}

	public ComputeProperties(String path)
	{
		// set path
		this.path = path;
		
		// prepend path to defaults
		updateDefaults(path);
		
		createPropertiesFile();

		// parse properties file
		parseProperties();

		// save new config
		saveProperties();
	}

	private void updateDefaults(String path)
	{
		this.workFlow = updatePath(path, this.workFlow);
		this.defaults = updatePath(path, this.defaults);
		this.runDir = updatePath(path, this.runDir);

		ArrayList<String> pathParameters = new ArrayList<String>();
		for (String p : this.parameters)
			pathParameters.add(updatePath(path, p));
		this.parameters = pathParameters.toArray(new String[pathParameters.size()]);
	}

	/**
	 * Prepend path if fileName has no absolute path
	 * 
	 * @param path
	 * @param fileName
	 * @return
	 */
	private String updatePath(String path, String fileName)
	{
		if (fileName.startsWith("/")) return fileName;
		else return path + (path.endsWith("/") ? "" : "/") + fileName;
	}

	private void setPath(String[] args)
	{
		Options options = createOptions();
		CommandLineParser parser = new PosixParser();
		CommandLine cmd;
		try
		{
			cmd = parser.parse(options, args);
			this.path = cmd.getOptionValue(Parameters.PATH_CMNDLINE_OPTION, this.path);
			this.path = this.path + (this.path.endsWith("/") ? "" : "/");
		}
		catch (ParseException e)
		{
			e.printStackTrace();
		}
	}

	public void createPropertiesFile()
	{
		// get location properties file
		String propFileString = path + File.separator + Parameters.PROPERTIES;

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
			this.path = p.getProperty(Parameters.PATH, this.path);
			this.workFlow = p.getProperty(Parameters.WORKFLOW, this.workFlow);
			this.defaults = p.getProperty(Parameters.DEFAULTS, this.defaults);
			this.backend = p.getProperty(Parameters.BACKEND, this.backend);
			this.runDir = p.getProperty(Parameters.RUNDIR, this.runDir);
			this.runId = p.getProperty(Parameters.RUNID, this.runId);
			this.database = p.getProperty(Parameters.DATABASE, this.database);

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
			this.path = cmd.getOptionValue(Parameters.PATH_CMNDLINE_OPTION, this.path);
			this.workFlow = getFullPath(cmd, Parameters.WORKFLOW_CMNDLINE_OPTION, this.workFlow);
			this.defaults = getFullPath(cmd, Parameters.DEFAULTS, this.defaults);
			this.backend = cmd.getOptionValue(Parameters.BACKEND_CMNDLINE_OPTION, this.backend);
			this.runDir = getFullPath(cmd, Parameters.RUNDIR_CMNDLINE_OPTION, this.runDir);
			this.runId = cmd.getOptionValue(Parameters.RUNID_CMNDLINE_OPTION, this.runId);
			this.database = cmd.getOptionValue(Parameters.DATABASE_CMNDLINE_OPTION, this.database);

			String[] cmdParameters = cmd.getOptionValues(Parameters.PARAMETERS_CMNDLINE_OPTION);
			cmdParameters = getFullPath(cmdParameters);
			if (null != cmdParameters) this.parameters = cmdParameters;
		}
		catch (ParseException e)
		{
			System.err.println(e.getMessage() + "\n");
			new HelpFormatter().printHelp("compute -p parameters.csv", options);
		}
	}

	private String[] getFullPath(String[] cmdParameters)
	{
		if (null == cmdParameters) return null;

		ArrayList<String> pathParameters = new ArrayList<String>();
		for (String p : cmdParameters)
			pathParameters.add(updatePath(this.path, p));
		return pathParameters.toArray(new String[pathParameters.size()]);
	}

	/**
	 * Returns path as specified by this cmndlineOption. If path is relative,
	 * then this.path/ will be prepended.
	 * 
	 * @param cmd
	 * @param cmndlineOption
	 * @param defaultValue
	 * @return
	 */
	private String getFullPath(CommandLine cmd, String cmndlineOption, String defaultValue)
	{
		String option = cmd.getOptionValue(cmndlineOption);
		if (null == option)
		{
			return defaultValue;
		}
		else
		{
			return updatePath(this.path, option);
		}
	}

	public void saveProperties()
	{
		Properties p = new Properties();
		try
		{
			// set this.variables
			p.setProperty(Parameters.PATH, this.path);
			p.setProperty(Parameters.WORKFLOW, this.workFlow);
			p.setProperty(Parameters.DEFAULTS, this.defaults);
			p.setProperty(Parameters.BACKEND, this.backend);
			p.setProperty(Parameters.RUNDIR, this.runDir);
			p.setProperty(Parameters.RUNID, this.runId);
			p.setProperty(Parameters.DATABASE, this.database);
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
		Option d = OptionBuilder.hasArg().withLongOpt(Parameters.PATH)
				.withDescription("Path to directory this generates to. Default: <current dir>.")
				.create(Parameters.PATH_CMNDLINE_OPTION);
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
