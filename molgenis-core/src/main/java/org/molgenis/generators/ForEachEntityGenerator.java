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
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;

/**
 * This generator applies the template to each entity. It uses defaults for template name, package name and classname:
 * <li>template name is this.getClass() + ".java.ftl" <li>package is {model.name}.{own package name}. For example,
 * org.molgenis.generate.foo.bar will be generated to {model.name}.foo.bar. <li>class name is own class name without
 * traling "Gen". For example: FooBarGen will generate {EntityName}FooBar.java files.
 * 
 * @author Morris Swertz
 * @since 30-jul-2007
 * 
 */
public abstract class ForEachEntityGenerator extends Generator
{
	public final transient Logger logger = Logger.getLogger(this.getClass());

	private boolean includeAbstract = false;

	private boolean handwritten = false;

	public ForEachEntityGenerator()
	{
		this(false);
	}

	public ForEachEntityGenerator(boolean includeAbstract)
	{
		this.includeAbstract = includeAbstract;
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		if (options.generate_tests)
		{
			// noop, override method in child class to generate tests
		}
		else
		{
			generate(model, options, false);
		}
	}

	protected void generate(Model model, MolgenisOptions options, boolean generateTests) throws Exception
	{
		Template template;
		String templatePostFix;
		if (generateTests)
		{
			template = this.createTemplate(this.getClass().getSimpleName().replace("Gen", "TestGen") + getExtension()
					+ ".ftl");
			templatePostFix = "Test";
		}
		else
		{
			template = this.createTemplate(this.getClass().getSimpleName() + getExtension() + ".ftl");
			templatePostFix = "";
		}

		Map<String, Object> templateArgs = createTemplateArguments(options);

		// apply generator to each entity
		for (Entity entity : model.getEntities())
		{
			// calculate package from its own package
			String packageName = entity.getNamespace().toLowerCase()
					+ this.getClass().getPackage().toString()
							.substring(Generator.class.getPackage().toString().length());
			File targetDir = new File(this.getSourcePath(options) + packageName.replace(".", "/"));
			if (handwritten) targetDir = new File(this.getHandWrittenPath(options) + packageName.replace(".", "/"));

			if (!entity.isImported() && (!entity.isAbstract() || this.includeAbstract)
					&& (!this.skipSystem() || !entity.isSystem()))
			{
				File targetFile = new File(targetDir + "/" + GeneratorHelper.getJavaName(entity.getName()) + getType()
						+ templatePostFix + getExtension());

				if (!handwritten || !targetFile.exists())
				{
					boolean created = targetDir.mkdirs();
					if (!created && !targetDir.exists())
					{
						throw new IOException("could not create " + targetDir);
					}

					templateArgs.put("entity", entity);
					templateArgs.put("model", model);
					templateArgs.put("template", template.getName());
					templateArgs.put("file", targetDir + "/" + GeneratorHelper.getJavaName(entity.getName())
							+ getType() + templatePostFix + getExtension());
					templateArgs.put("package", packageName);
					templateArgs.put("jpa_use_sequence", options.jpa_use_sequence);

					OutputStream targetOut = new FileOutputStream(targetFile);

					template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
					targetOut.close();

					logger.info("generated " + targetFile);
				}
			}
		}
	}

	/**
	 * Calculate class name from its own name.
	 * 
	 * 
	 * @return Name(this.getClass()) - "Gen"
	 */
	public String getType()
	{
		String className = this.getClass().getSimpleName();
		return className.substring(0, className.length() - 3);
	}

	/**
	 * Skip system entities.
	 */
	public Boolean skipSystem()
	{
		return Boolean.FALSE;
	}

	/**
	 * Whether this generator should generate to the handwritten folder insteadof generated folder
	 */
	public boolean isHandwritten()
	{
		return handwritten;
	}

	/**
	 * Whether this generator should generate to the handwritten folder insteadof generated folder
	 */
	public void setHandwritten(boolean handwritten)
	{
		this.handwritten = handwritten;
	}

	public boolean isIncludeAbstract()
	{
		return includeAbstract;
	}

	public void setIncludeAbstract(boolean includeAbstract)
	{
		this.includeAbstract = includeAbstract;
	}
}
