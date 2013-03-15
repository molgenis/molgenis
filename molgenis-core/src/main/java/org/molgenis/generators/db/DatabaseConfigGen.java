package org.molgenis.generators.db;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisOptions;
import org.molgenis.generators.Generator;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;

public class DatabaseConfigGen extends Generator
{
	private final static Logger logger = Logger.getLogger(DatabaseConfigGen.class);

	@Override
	public String getDescription()
	{
		return "Generates database configuration that can be used by a Spring container.";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		File target = new File(this.getSourcePath(options) + APP_DIR.replace('.', '/') + "/DatabaseConfig.java");
		boolean created = target.getParentFile().mkdirs();
		if (!created && !target.getParentFile().exists())
		{
			throw new IOException("could not create " + target.getParentFile());
		}

		Map<String, Object> templateArgs = createTemplateArguments(options);
		templateArgs.put("package", APP_DIR);
		templateArgs.put("databaseImp",
				options.mapper_implementation.equals(MolgenisOptions.MapperImplementation.JPA) ? "jpa" : "jdbc");
		templateArgs.put("db_mode", options.db_mode);
		templateArgs.put("db_driver", options.db_driver);
		templateArgs.put("db_uri", options.db_uri);
		templateArgs.put("db_user", options.db_user);
		templateArgs.put("db_password", options.db_password);
		templateArgs.put("auth_loginclass", options.auth_loginclass);

		Template template = createTemplate("/" + getClass().getSimpleName() + ".java.ftl");
		OutputStream targetOut = new FileOutputStream(target);
		try
		{
			template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
		}
		finally
		{
			targetOut.close();
		}

		logger.info("generated " + target);
	}
}
