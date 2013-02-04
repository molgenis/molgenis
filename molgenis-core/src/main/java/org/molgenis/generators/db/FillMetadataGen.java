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

public class FillMetadataGen extends Generator
{
	private static final Logger logger = Logger.getLogger(FillMetadataGen.class);

	@Override
	public String getDescription()
	{
		return "Generates one FillMetadata class that will insert meta data into the database (for auth)";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		Template template = createTemplate("/" + getClass().getSimpleName() + ".java.ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		List<Entity> entityList = model.getEntities();
		// this.sortEntitiesByXref(entityList,model); //side effect?
		File target = new File(this.getSourcePath(options) + APP_DIR + "/FillMetadata.java");
		boolean created = target.getParentFile().mkdirs();
		if (!created && !target.getParentFile().exists())
		{
			throw new IOException("could not create " + target.getParentFile());
		}

		templateArgs.put("model", model);
		templateArgs.put("entities", entityList);
		templateArgs.put("package", APP_DIR);
		templateArgs.put("auth_loginclass", options.auth_loginclass);
		templateArgs.put("decorator_overriders", options.decorator_overriders);
		templateArgs.put("metaData", !options.auth_loginclass.endsWith("SimpleLogin"));
		templateArgs.put("databaseImpl", options.mapper_implementation);

		OutputStream targetOut = new FileOutputStream(target);
		template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
		targetOut.close();
		
		logger.info("generated " + target);
	}

}
