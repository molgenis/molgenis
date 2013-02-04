package org.molgenis.generators.server;

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

public class MolgenisContextListenerGen extends Generator
{
	private static final Logger logger = Logger.getLogger(MolgenisContextListenerGen.class);

	@Override
	public String getDescription()
	{
		return "Generates a context listener for MOLGENIS.";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		Template template = createTemplate(this.getClass().getSimpleName() + ".ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		File target = new File(this.getSourcePath(options) + APP_DIR + "/servlet/MolgenisContextListener.java");
		boolean created = target.getParentFile().mkdirs();
		if (!created && !target.getParentFile().exists())
		{
			throw new IOException("could not create " + target.getParentFile());
		}

		templateArgs.put("model", model);
		templateArgs.put("package", APP_DIR);
		templateArgs.put("db_filepath", options.db_filepath);
		templateArgs.put("db_user", options.db_user);
		templateArgs.put("db_password", options.db_password);
		templateArgs.put("db_uri", options.db_uri);
		templateArgs.put("db_driver", options.db_driver);
		templateArgs.put("db_jndiname", options.db_jndiname);

		OutputStream targetOut = new FileOutputStream(target);
		template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
		targetOut.close();

		logger.info("generated " + target);
	}
}
