package org.molgenis.generators.doc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisOptions;
import org.molgenis.generators.Generator;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;
import org.molgenis.model.elements.Module;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public class DotDocMinimalGen extends Generator
{
	private static final Logger logger = Logger.getLogger(DotDocMinimalGen.class);

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
		Template template = createTemplate("/" + getClass().getSimpleName() + ".java.ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		File target = new File(this.getDocumentationPath(options) + "/objectmodel-uml-diagram-summary.dot");
		boolean created = target.getParentFile().mkdirs();
		if (!created && !target.getParentFile().exists())
		{
			throw new IOException("could not create " + target.getParentFile());
		}

		List<Entity> entityList = model.getEntities();
		// MolgenisLanguage.sortEntitiesByDependency(entityList, model);
		templateArgs.put("model", model);
		templateArgs.put("module", model);
		templateArgs.put("entities", entityList);
		apply(templateArgs, template, target);
		logger.info("generated " + target);
		executeDot(target, "png");

		// repeat for each package
		for (Module module : model.getModules())
		{
			entityList = module.getEntities();
			templateArgs.put("model", model);
			templateArgs.put("module", module);
			templateArgs.put("entities", entityList);
			target = new File(this.getDocumentationPath(options) + "/objectmodel-uml-diagram-summary-"
					+ module.getName() + ".dot");
			apply(templateArgs, template, target);

			executeDot(target, "png");
			logger.info("generated " + target);
		}
	}

	private void apply(Map<String, Object> templateArgs, Template template, File target) throws IOException,
			TemplateException
	{

		OutputStream targetOut = new FileOutputStream(target);
		template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
		targetOut.close();
	}

	private void executeDot(File dotFile, String type)
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
			{
				// command flags infile outfile
				command += "" + GRAPHVIZ_COMMAND_WINDOWS + " -T" + type + " -O \"" + dotFile.getAbsolutePath() + "\"";
			}
			logger.debug("Executing: " + command);
			Runtime.getRuntime().exec(command);
			logger.debug("Data model image was generated succesfully.\nOutput:\n" + result);
		}
		catch (Exception e)
		{
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
