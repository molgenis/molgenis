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

public class UsedMolgenisOptionsGen extends Generator
{
	private static final Logger logger = Logger.getLogger(UsedMolgenisOptionsGen.class);

	@Override
	public String getDescription()
	{
		return "Generates a Java class with a HashMap containing all used MolgenisOptions to be used at runtime.";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		Template template = createTemplate(this.getClass().getSimpleName() + ".ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);
		templateArgs.put("package", APP_DIR);
		templateArgs.put("options", options);
		templateArgs.put("model", model);

		File target = new File(this.getSourcePath(options) + APP_DIR + "/servlet/UsedMolgenisOptions.java");
		boolean created = target.getParentFile().mkdirs();
		if (!created && !target.getParentFile().exists())
		{
			throw new IOException("could not create " + target.getParentFile());
		}

		OutputStream targetOut = new FileOutputStream(target);
		template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
		targetOut.close();

		logger.info("generated " + target);
	}
}
