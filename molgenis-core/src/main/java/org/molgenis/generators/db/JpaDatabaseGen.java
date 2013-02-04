package org.molgenis.generators.db;

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

import freemarker.template.Template;

public class JpaDatabaseGen extends Generator
{
	private final static Logger logger = Logger.getLogger(JpaDatabaseGen.class);

	@Override
	public String getDescription()
	{
		return "Generates one Jpa to talk to the data. Encapsulates Database Mappers to do this.";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		Template template = createTemplate("/" + getClass().getSimpleName() + ".java.ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		List<Entity> entityList = model.getEntities();
		// this.sortEntitiesByXref(entityList,model); //side effect?

		File target = new File(this.getSourcePath(options) /*
															 * +
															 * model.getName().
															 * toLowerCase
															 * ().replace(".",
															 * "/")
															 */+ "/app/JpaDatabase.java");
		boolean created = target.getParentFile().mkdirs();
		if (!created && !target.getParentFile().exists())
		{
			throw new IOException("could not create " + target.getParentFile());
		}

		templateArgs.put("model", model);
		templateArgs.put("entities", entityList);
		String packageName = model.getName().toLowerCase();
		templateArgs.put("package", packageName);
		templateArgs.put("auth_loginclass", options.auth_loginclass);
		templateArgs.put("disable_decorators", options.disable_decorators);
		OutputStream targetOut = new FileOutputStream(target);
		template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
		targetOut.close();

		logger.info("generated " + target);
	}

}
