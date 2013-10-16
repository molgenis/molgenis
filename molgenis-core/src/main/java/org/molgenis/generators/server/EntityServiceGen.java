package org.molgenis.generators.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisOptions;
import org.molgenis.generators.Generator;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;

public class EntityServiceGen extends Generator
{
	private static final Logger logger = Logger.getLogger(EntityServiceGen.class);

	@Override
	public String getDescription()
	{
		return "Generates REST service interfaces for each entity.";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		if (options.generate_tests == true)
		{
			generateServiceTests(model, options);
		}
		else
		{
			generateServices(model, options);
		}
	}

	private void generateServices(Model model, MolgenisOptions options) throws Exception
	{
		Template template = createTemplate("/EntityServiceGen.java.ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		for (Entity entity : model.getEntities())
		{
			// skip abstract and system entities
			if (entity.isAbstract()) continue;
			templateArgs.put("entity", entity);

			File generatedFile = new File(this.getSourcePath(options) + "org/molgenis/service/" + entity.getName()
					+ "Service.java");
			boolean created = generatedFile.getParentFile().mkdirs();
			if (!created && !generatedFile.getParentFile().exists())
			{
				throw new IOException("could not create " + generatedFile.getParentFile());
			}

			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(generatedFile),
					Charset.forName("UTF-8"));
			try
			{
				template.process(templateArgs, writer);
			}
			finally
			{
				writer.close();
			}

			logger.info("generated " + generatedFile);
		}
	}

	private void generateServiceTests(Model model, MolgenisOptions options) throws Exception
	{
		Template template = createTemplate("/EntityServiceTestGen.java.ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		for (Entity entity : model.getEntities())
		{
			// skip abstract and system entities
			if (entity.isAbstract()) continue;
			templateArgs.put("entity", entity);

			File generatedFile = new File(this.getSourcePath(options) + "org/molgenis/service/" + entity.getName()
					+ "ServiceTest.java");
			boolean created = generatedFile.getParentFile().mkdirs();
			if (!created && !generatedFile.getParentFile().exists())
			{
				throw new IOException("could not create " + generatedFile.getParentFile());
			}

			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(generatedFile),
					Charset.forName("UTF-8"));
			try
			{
				template.process(templateArgs, writer);
			}
			finally
			{
				writer.close();
			}

			logger.info("generated " + generatedFile);
		}
	}
}