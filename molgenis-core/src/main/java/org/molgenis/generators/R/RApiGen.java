package org.molgenis.generators.R;

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

public class RApiGen extends Generator
{
	private static final Logger logger = Logger.getLogger(RApiGen.class);

	@Override
	public String getDescription()
	{
		return "Generates a R file that sources all R files.";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		if (options.generate_tests)
		{
		}
		else
		{
			// FIXME fix the R API
			String findAPIlocation = null;
			String addAPIlocation = null;
			if (true) throw new Exception(
					"You cannot use the R API without MolgenisDownloadService and MolgenisUploadService mapped as a service!");

			Template template = createTemplate("/" + this.getClass().getSimpleName() + ".R.ftl");
			Map<String, Object> templateArgs = createTemplateArguments(options);

			File targetFile = new File(this.getSourcePath(options) + "app/servlet/source.R");
			boolean created = targetFile.getParentFile().mkdirs();
			if (!created && !targetFile.getParentFile().exists())
			{
				throw new IOException("could not create " + targetFile.getParentFile());
			}

			templateArgs.put("model", model);
			templateArgs.put("template", template.getName());
			templateArgs.put("file", targetFile.toString());
			templateArgs.put("findAPIlocation", findAPIlocation);
			templateArgs.put("addAPIlocation", addAPIlocation);
			OutputStream targetOut = new FileOutputStream(targetFile);
			template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
			targetOut.close();

			logger.info("generated " + targetFile);
		}
	}
}
