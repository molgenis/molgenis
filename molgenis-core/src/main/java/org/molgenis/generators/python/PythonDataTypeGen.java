package org.molgenis.generators.python;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

import org.molgenis.MolgenisOptions;
import org.molgenis.generators.ForEachEntityGenerator;
import org.molgenis.generators.Generator;
import org.molgenis.generators.GeneratorHelper;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;

public class PythonDataTypeGen extends ForEachEntityGenerator
{

	private boolean includeAbstract = false;

	private boolean handwritten = false;

	public PythonDataTypeGen()
	{
		// include abstract entities
		super(true);
	}

	@Override
	public String getDescription()
	{
		return "Generates Python classes for each entity.";
	}

	@Override
	public String getType()
	{
		return "";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		Template template = this.createTemplate(this.getClass().getSimpleName() + getExtension() + ".ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		// logger.error("packageName:" + "ENTITY"+
		// this.getClass().getPackage().toString().substring(Generator.class.getPackage().toString().length()));

		// apply generator to each entity
		for (Entity entity : model.getEntities())
		{
			// calculate package from its own package
			String packageName = entity.getNamespace().toLowerCase()
					+ this.getClass().getPackage().toString()
							.substring(Generator.class.getPackage().toString().length());
			File targetDir = new File(this.getPythonSourcePath(options)
					+ packageName.replace(".", "/").replace("/python", ""));
			if (handwritten) targetDir = new File(this.getHandWrittenPath(options)
					+ packageName.replace(".", "/").replace("/python", ""));

			boolean created = targetDir.mkdirs();
			if (!created && !targetDir.exists())
			{
				throw new IOException("could not create " + targetDir);
			}

			try
			{
				if ((!entity.isAbstract() || this.includeAbstract) && (!this.skipSystem() || !entity.isSystem()))
				{
					File targetFile = new File(targetDir + "/" + GeneratorHelper.firstToUpper(entity.getName())
							+ getType() + getExtension());

					// logger.error("targetDir: " +targetDir.getAbsolutePath());
					// logger.error(" GeneratorHelper.firstToUpper(entity.getName()): "
					// + GeneratorHelper.firstToUpper(entity.getName()));
					// logger.error("getType() + getExtension(): " +getType() +
					// getExtension());
					// logger.error("targetFile: " +
					// targetFile.getAbsolutePath());
					if (!handwritten || !targetFile.exists())
					{

						// logger.debug("trying to generated "+targetFile);
						templateArgs.put("entity", entity);
						templateArgs.put("model", model);
						templateArgs.put("db_driver", options.db_driver);
						templateArgs.put("template", template.getName());
						templateArgs.put("file", targetDir + "/" + GeneratorHelper.firstToUpper(entity.getName())
								+ getType() + getExtension());
						templateArgs.put("package", packageName);

						OutputStream targetOut = new FileOutputStream(targetFile);

						template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
						targetOut.close();

						// logger.info("generated " +
						// targetFile.getAbsolutePath());
						logger.info("generated " + targetFile);
					}
				}
			}
			catch (Exception e)
			{
				logger.error("problem generating for " + entity.getName());
				throw e;
			}
		}
	}

	@Override
	public String getExtension()
	{
		return ".py";
	}

}
