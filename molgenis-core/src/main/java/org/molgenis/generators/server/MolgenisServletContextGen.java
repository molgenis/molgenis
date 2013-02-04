package org.molgenis.generators.server;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisOptions;
import org.molgenis.generators.Generator;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;

public class MolgenisServletContextGen extends Generator
{
	private static final Logger logger = Logger.getLogger(MolgenisServletContextGen.class);

	@Override
	public String getDescription()
	{
		return "Generates the central servlet for MOLGENIS.";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		Template template = createTemplate(this.getClass().getSimpleName() + ".ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		File target = new File(this.getWebserverPath(options) + "/META-INF/context.xml");
		target.getParentFile().mkdirs();

		templateArgs.put("model", model);
		templateArgs.put("package", model.getName().toLowerCase());
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
