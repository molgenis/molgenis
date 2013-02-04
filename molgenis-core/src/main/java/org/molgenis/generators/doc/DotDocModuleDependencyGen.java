package org.molgenis.generators.doc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisOptions;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.XrefField;
import org.molgenis.generators.Generator;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Field;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public class DotDocModuleDependencyGen extends Generator
{
	private static final Logger logger = Logger.getLogger(DotDocModuleDependencyGen.class);

	// need to add input and output file
	public static final String GRAPHVIZ_COMMAND_WINDOWS = "dot";

	@Override
	public String getDescription()
	{
		return "Generates one documentation file describing all entities.";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		this.generate(model, options, false);
	}

	public void generate(Model model, MolgenisOptions options, boolean wait) throws Exception
	{
		Template template = createTemplate("/" + getClass().getSimpleName() + ".java.ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		File target = new File(this.getDocumentationPath(options) + "/module-dependency-diagram.dot");
		boolean created = target.getParentFile().mkdirs();
		if (!created && !target.getParentFile().exists())
		{
			throw new IOException("could not create " + target.getParentFile());
		}

		// count the relationships
		Map<String, Integer> mapOfRelations = new LinkedHashMap<String, Integer>();
		for (Entity e : model.getEntities())
		{
			if (e.getModel() != null)
			{
				// interface
				if (e.hasImplements())
				{
					for (Entity i : e.getImplements())
					{
						if (i.getModule() != null)
						{
							addRule(mapOfRelations, e, i);
						}
					}
				}
				// superclass
				if (e.hasAncestor())
				{
					addRule(mapOfRelations, e, e.getAncestor());
				}
				// xrefs/mrefs
				for (Field f : e.getFields())
				{
					if (f.getType() instanceof XrefField || f.getType() instanceof MrefField)
					{
						addRule(mapOfRelations, e, f.getXrefEntity());
					}
				}

			}
		}

		templateArgs.put("rules", mapOfRelations);

		apply(templateArgs, template, target);
		logger.info("generated " + target);
		executeDot(target, "png", wait);

	}

	private void addRule(Map<String, Integer> mapOfRelations, Entity e, Entity i)
	{
		if (e.getModule() != null && i.getModule() != null && e.getModule() != i.getModule())
		{
			String rule = "\"" + e.getModule().getName() + "\"->\"" + i.getModule().getName() + "\"";
			if (!mapOfRelations.containsKey(rule)) mapOfRelations.put(rule, 0);
			mapOfRelations.put(rule, mapOfRelations.get(rule) + 1);
			logger.info(rule);
		}

	}

	private void apply(Map<String, Object> templateArgs, Template template, File target) throws IOException,
			TemplateException
	{

		OutputStream targetOut = new FileOutputStream(target);
		template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
		targetOut.close();
	}

	private void executeDot(File dotFile, String type, boolean wait)
	{
		// write script to disc
		String command = "";
		// String error = "";
		String result = "";
		// String output = "";
		// File inputfile = null;
		// File outputfile = null;
		try
		{

			// execute the scripts
			// if
			// (System.getProperty("os.name").toLowerCase().indexOf("windows")
			// == -1)
			// {
			// // make tempfiles executable
			// // command = "chmod 777 "+inputfile.getCanonicalPath()+"\n";
			// // logger.debug("added chmod 777 on input file");
			// command += GRAPHVIZ_COMMAND_WINDOWS;
			// }
			// else
			// windows
			// command flags infile outfile
			command += "" + GRAPHVIZ_COMMAND_WINDOWS + " -T" + type + " -O \"" + dotFile.getAbsolutePath() + "\"";

			Process p;
			String os = System.getProperty("os.name").toLowerCase();

			if (os.indexOf("windows 9") > -1)
			{
				p = Runtime.getRuntime().exec(new String[]
				{ "command.com", "/c", command });
			}
			else if (os.indexOf("windows") > -1)
			{
				p = Runtime.getRuntime().exec(new String[]
				{ "cmd.exe", "/c", command });
			}
			else
			{
				p = Runtime.getRuntime().exec(new String[]
				{ "/bin/sh", "-c", command });
			}

			logger.debug("Executing: " + command);
			if (wait) p.waitFor();
			logger.debug("Data model image was generated succesfully.\nOutput:\n" + result);

			{
				// command flags infile outfile
				command = "" + GRAPHVIZ_COMMAND_WINDOWS + " -Tsvg" + " -O \"" + dotFile.getAbsolutePath() + "\"";
			}
			logger.debug("Executing: " + command);

			if (os.indexOf("windows 9") > -1)
			{
				p = Runtime.getRuntime().exec(new String[]
				{ "command.com", "/c", command });
			}
			else if (os.indexOf("windows") > -1)
			{
				p = Runtime.getRuntime().exec(new String[]
				{ "cmd.exe", "/c", command });
			}
			else
			{
				p = Runtime.getRuntime().exec(new String[]
				{ "/bin/sh", "-c", command });
			}
			if (wait) p.waitFor();
			logger.debug("Data model image was generated succesfully.\nOutput:\n" + result);

		}
		catch (IOException e)
		{
			e.printStackTrace();
			logger.error("Generation of graphical documentation failed: return code " + e.getMessage()
					+ ". Install GraphViz and put dot.exe on your path.");
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
			logger.error("Generation of graphical documentation failed: return code " + e.getMessage()
					+ ". Install GraphViz and put dot.exe on your path.");
		}
		finally
		{
			// inputfile.delete();
			// outputfile.delete();
		}
	}

	/** Helper function to translate streams to strings */
	// private String streamToString(InputStream inputStream) throws IOException
	// {
	// StringBuffer fileContents = new StringBuffer();
	// BufferedReader reader = new BufferedReader(new
	// InputStreamReader(inputStream));
	// String line;
	// while ((line = reader.readLine()) != null)
	// {
	// fileContents.append(line + "\n");
	// }
	// reader.close();
	// inputStream.close();
	// return fileContents.toString();
	// }

}
