package org.molgenis.generators.tests;

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

public class TestCsvGen extends Generator
{
	private static final Logger logger = Logger.getLogger(TestCsvGen.class);

	@Override
	public String getDescription()
	{
		return "Generates simple junit test that creates a database and updates/queries data.";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		if (options.generate_tests)
		{
			Template template = createTemplate("/" + this.getClass().getSimpleName() + ".java.ftl");
			Map<String, Object> templateArgs = createTemplateArguments(options);

			List<Entity> entityList = model.getEntities();
			entityList = MolgenisModel.sortEntitiesByDependency(entityList, model); // side
																					// effect?

			File target = new File(this.getSourcePath(options) + "/test/TestCsv.java");
			boolean created = target.getParentFile().mkdirs();
			if (!created && !target.getParentFile().exists())
			{
				throw new IOException("could not create " + target.getParentFile());
			}

			String packageName = "test";

			templateArgs.put("databaseImp",
					options.mapper_implementation.equals(MolgenisOptions.MapperImplementation.JPA) ? "jpa" : "jdbc");
			templateArgs.put("model", model);
			templateArgs.put("db_mode", options.db_mode);
			templateArgs.put("entities", entityList);
			templateArgs.put("package", packageName);
			templateArgs.put("options", options);

			OutputStream targetOut = new FileOutputStream(target);
			template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
			targetOut.close();

			logger.info("generated " + target);
		}
		else
		{
		}
	}
}
