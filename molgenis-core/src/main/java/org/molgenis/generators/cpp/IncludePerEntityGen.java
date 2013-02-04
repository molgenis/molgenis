package org.molgenis.generators.cpp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.MolgenisOptions;
import org.molgenis.generators.ForEachEntityGenerator;
import org.molgenis.generators.Generator;
import org.molgenis.generators.GeneratorHelper;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;

public class IncludePerEntityGen extends ForEachEntityGenerator
{
	private static final Logger logger = Logger.getLogger(IncludePerEntityGen.class);

	@Override
	public String getDescription()
	{
		return "Generate CPP header files";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		Template template = this.createTemplate(this.getClass().getSimpleName() + getExtension() + ".ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		// apply generator to each entity
		for (Entity entity : model.getEntities())
		{
			// calculate package from its own package
			String packageName = entity.getNamespace().toLowerCase()
					+ this.getClass().getPackage().toString()
							.substring(Generator.class.getPackage().toString().length());

			File targetDir = new File((this.getSourcePath(options).endsWith("/") ? this.getSourcePath(options)
					: this.getSourcePath(options) + "/")
					+ packageName.replace(".", "/").replace("/cpp", ""));
			try
			{
				File targetFile = new File(targetDir + "/" + GeneratorHelper.getJavaName(entity.getName())
						+ getExtension());
				boolean created = targetDir.mkdirs();
				if (!created && !targetDir.exists())
				{
					throw new IOException("could not create " + targetDir);
				}

				// logger.debug("trying to generated "+targetFile);
				templateArgs.put("entity", entity);
				templateArgs.put("model", model);
				templateArgs.put("db_driver", options.db_driver);
				templateArgs.put("template", template.getName());
				templateArgs.put(
						"file",
						targetDir.getCanonicalPath().replace("\\", "/") + "/"
								+ GeneratorHelper.firstToUpper(entity.getName()) + getType() + getExtension());
				templateArgs.put("package", packageName);

				OutputStream targetOut = new FileOutputStream(targetFile);

				template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
				targetOut.close();
				logger.info("generated " + targetFile);
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
		return ".h";
	}

	@Override
	public String getSourcePath(MolgenisOptions options)
	{
		return options.output_cpp;
	}

}
