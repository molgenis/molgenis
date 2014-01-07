package org.molgenis.generators;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisOptions;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;

/**
 * Generates org.molgenis.data.jpa.JpaEntitySourceImpl
 * 
 * For now we have only 1 JpaEntitySource per application
 * 
 */
public class JpaEntitySourceGen extends Generator
{
	private static Logger logger = Logger.getLogger(JpaEntitySourceGen.class);

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		if (options.generate_tests)
		{
			return;
		}

		Template template = createTemplate("/" + getClass().getSimpleName() + ".java.ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		File target = new File(getSourcePath(options) + "/org/molgenis/data/jpa/JpaEntitySourceImpl.java");

		boolean created = target.getParentFile().mkdirs();
		if (!created && !target.getParentFile().exists())
		{
			throw new IOException("could not create " + target.getParentFile());
		}

		templateArgs.put("options", options);
		templateArgs.put("model", model);
		templateArgs.put("disable_decorators", options.disable_decorators);

		OutputStream targetOut = new FileOutputStream(target);
		template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
		targetOut.close();

		logger.info("generated " + target);
	}

	@Override
	public String getDescription()
	{
		return "Generates org.molgenis.data.jpa.JpaEntitySourceImpl";
	}

}
