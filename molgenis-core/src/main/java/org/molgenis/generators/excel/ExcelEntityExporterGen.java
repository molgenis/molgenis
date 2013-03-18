package org.molgenis.generators.excel;

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
import org.molgenis.generators.sql.MySqlCreateClassPerTableGen;
import org.molgenis.model.MolgenisModel;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;

public class ExcelEntityExporterGen extends MySqlCreateClassPerTableGen
{
	private static final Logger logger = Logger.getLogger(ExcelEntityExporterGen.class);

	@Override
	public String getDescription()
	{
		return "Generates ExcelEntityExporter";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		if (options.generate_tests)
		{
		}
		else
		{
			Template template = createTemplate("/" + this.getClass().getSimpleName() + ".java.ftl");
			Map<String, Object> templateArgs = createTemplateArguments(options);

			List<Entity> entityList = MolgenisModel.sortEntitiesByDependency(model.getEntities(), model); // side
																											// effect?

			File target = new File(this.getSourcePath(options) + APP_DIR + "/ExcelEntityExporter.java");
			boolean created = target.getParentFile().mkdirs();
			if (!created && !target.getParentFile().exists())
			{
				throw new IOException("could not create " + target.getParentFile());
			}

			templateArgs.put("model", model);
			templateArgs.put("entities", entityList);
			templateArgs.put("package", APP_DIR);
			OutputStream targetOut = new FileOutputStream(target);
			template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
			targetOut.close();

			logger.info("generated " + target);
		}
	}
}
