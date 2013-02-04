package org.molgenis.generators.cpp;

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
import org.molgenis.generators.ForEachEntityGenerator;
import org.molgenis.model.MolgenisModel;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;

public class CPPMainGen extends ForEachEntityGenerator
{
	private static final Logger logger = Logger.getLogger(CPPMainGen.class);

	@Override
	public String getDescription()
	{
		return "Generates CPP cmake file";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		Template template = createTemplate("/" + this.getClass().getSimpleName() + ".cpp.ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		List<Entity> entityList = model.getEntities();
		MolgenisModel.sortEntitiesByDependency(entityList, model);
		File target = new File(this.getCPPSourcePath(options) + "/main.cpp");
		boolean created = target.getParentFile().mkdirs();
		if (!created && !target.getParentFile().exists())
		{
			throw new IOException("could not create " + target.getParentFile());
		}

		templateArgs.put("model", model);
		templateArgs.put("entities", entityList);
		templateArgs.put("UserHome", System.getProperty("user.dir").replace("\\", "/").toString());
		OutputStream targetOut = new FileOutputStream(target);
		template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
		targetOut.close();
		logger.info("generated " + target);
	}

	@Override
	public String getSourcePath(MolgenisOptions options)
	{
		return options.output_cpp;
	}

}
