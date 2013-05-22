package org.molgenis.generators.csv;

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
import org.molgenis.model.MolgenisModel;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;

public class CsvEntityImporterGen extends Generator
{
	private static final Logger logger = Logger.getLogger(CsvEntityImporterGen.class);

	@Override
	public String getDescription()
	{
		return "Generates CsvEntityImporter";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		if (options.generate_tests)
		{
		}
		else
		{
			Template template = createTemplate("/" + this.getClass().getSimpleName() + ".java.ftl");
			Map<String, Object> templateArgs = createTemplateArguments(options);

			List<Entity> entityList = model.getEntities();
			entityList = MolgenisModel.sortEntitiesByDependency(entityList, model); // side
																					// effect?

			File target = new File(this.getSourcePath(options) + APP_DIR + "/CsvEntityImporterImpl.java");
			boolean created = target.getParentFile().mkdirs();
			if (!created && !target.getParentFile().exists())
			{
				throw new IOException("could not create " + target.getParentFile());
			}

			templateArgs.put("model", model);
			templateArgs.put("entities", entityList);
			templateArgs.put("package", APP_DIR.replace('/', '.'));
			OutputStream targetOut = new FileOutputStream(target);
			template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
			targetOut.close();

			logger.info("generated " + target);
		}
	}

	@Override
	public void generate(Model model, MolgenisOptions options, String target) throws Exception
	{
		Template template = createTemplate(this.getClass().getSimpleName() + ".java.ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		List<Entity> entityList = model.getEntities();
		entityList = MolgenisModel.sortEntitiesByDependency(entityList, model); // side
		// effect?

		File generatedJavaFile = new File(target);
		boolean created = generatedJavaFile.getParentFile().mkdirs();
		if (!created && !generatedJavaFile.getParentFile().exists())
		{
			throw new IOException("could not create " + generatedJavaFile.getParentFile());
		}

		templateArgs.put("model", model);
		templateArgs.put("entities", entityList);
		templateArgs.put("package", this.getClass().getPackage().getName());
		OutputStream targetOut = new FileOutputStream(target);
		template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
		targetOut.close();

		logger.info("generated " + target);
	}
}
