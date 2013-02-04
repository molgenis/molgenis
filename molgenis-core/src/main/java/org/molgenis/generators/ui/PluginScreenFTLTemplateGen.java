package org.molgenis.generators.ui;

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
import org.molgenis.model.elements.Plugin;
import org.molgenis.model.elements.UISchema;

import freemarker.template.Template;

public class PluginScreenFTLTemplateGen extends Generator
{
	private static final Logger logger = Logger.getLogger(PluginScreenFTLTemplateGen.class);

	@Override
	public String getDescription()
	{
		return "Generates in the handwritten folder a template of an FTL.";
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		generateForm(model, options, model.getUserinterface());
	}

	private void generateForm(Model model, MolgenisOptions options, UISchema schema) throws Exception
	{
		Template template = createTemplate("/" + getClass().getSimpleName() + ".ftl.ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		for (UISchema screen : schema.getChildren())
		{
			if (screen.getClass() == Plugin.class)
			{

				Plugin plugin = (Plugin) screen;

				String fullKlazzName = plugin.getPluginType();
				String packageName = fullKlazzName;
				if (fullKlazzName.contains(".")) packageName = fullKlazzName.substring(0,
						fullKlazzName.lastIndexOf("."));

				String shortKlazzName = fullKlazzName;
				if (fullKlazzName.contains(".")) shortKlazzName = fullKlazzName.substring(fullKlazzName
						.lastIndexOf(".") + 1);

				File targetFile = new File(this.getHandWrittenPath(options) + "/" + fullKlazzName.replace(".", "/")
						+ ".ftl");
				// only generate if the file doesn't exist AND plugin not
				// already on classpath
				Class<?> c = null;
				try
				{
					c = Class.forName(fullKlazzName);
					// return;
				}
				catch (ClassNotFoundException e)
				{
					logger.error("skipped plugin " + plugin.getName() + " as it is on the classpath");
				}
				logger.error("tested classforname on " + fullKlazzName + ": " + c);

				if (!targetFile.exists() && c == null)
				{
					File targetDir = new File(this.getHandWrittenPath(options) + "/" + packageName.replace(".", "/"));
					boolean created = targetDir.mkdirs();
					if (!created && !targetDir.exists())
					{
						throw new IOException("could not create " + targetDir);
					}

					templateArgs.put("screen", plugin);
					templateArgs.put("template", template.getName());
					templateArgs.put("clazzName", shortKlazzName);
					templateArgs.put("macroName", fullKlazzName.replace(".", "_"));
					templateArgs.put("templatePath", targetFile.toString().replace("\\", "/"));
					templateArgs.put("package", packageName);

					OutputStream targetOut = new FileOutputStream(targetFile);
					template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
					targetOut.close();

					logger.info("generated "
							+ targetFile.getAbsolutePath().substring(this.getHandWrittenPath(options).length()));
				}
				else
				{
					logger.warn("Skipped because exists: " + targetFile);
				}
			}

			// get children of this screen and generate those
			generateForm(model, options, screen);
		}
	}
}
