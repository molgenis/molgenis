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
import org.molgenis.model.elements.Matrix;
import org.molgenis.model.elements.Model;

import freemarker.template.Template;

/**
 * This generator applies the template to each entity. It uses defaults for
 * template name, package name and classname: <li>template name is
 * this.getClass() + ".java.ftl" <li>package is {model.name}.{own package name}.
 * For example, org.molgenis.generate.foo.bar will be generated to
 * {model.name}.foo.bar. <li>class name is own class name without traling "Gen".
 * For example: FooBarGen will generate {EntityName}FooBar.java files.
 * 
 * @author Morris Swertz
 * @since 30-jul-2007
 * 
 */
public abstract class ForEachMatrixGenerator extends Generator
{
	private static final Logger logger = Logger.getLogger(ForEachMatrixGenerator.class);

	// private boolean includeAbstract = false;

	public ForEachMatrixGenerator()
	{
		this(false);
	}

	public ForEachMatrixGenerator(boolean includeAbstract)
	{
		// this.includeAbstract = includeAbstract;
	}

	@Override
	public void generate(Model model, MolgenisOptions options) throws Exception
	{
		Template template = this.createTemplate(this.getClass().getSimpleName() + getExtension() + ".ftl");
		Map<String, Object> templateArgs = createTemplateArguments(options);

		// calculate package from its own package
		String packageName = this.getClass().getPackage().toString()
				.substring(Generator.class.getPackage().toString().length());
		File targetDir = new File(getSourcePath(options) + model.getName().replace(".", "/")
				+ packageName.replace(".", "/"));

		// apply generator to each matrix
		for (Matrix matrix : model.getMatrices())
		{
			boolean created = targetDir.mkdirs();
			if (!created && !targetDir.exists())
			{
				throw new IOException("could not create " + targetDir);
			}

			File targetFile = new File(targetDir + "/" + GeneratorHelper.firstToUpper(matrix.getName()) + getType()
					+ getExtension());

			templateArgs.put("matrix", matrix);
			templateArgs.put("model", model);
			templateArgs.put("template", template.getName());
			templateArgs.put("file", targetFile.toString());
			templateArgs.put("package", model.getName().toLowerCase() + packageName);

			OutputStream targetOut = new FileOutputStream(targetFile);
			template.process(templateArgs, new OutputStreamWriter(targetOut, Charset.forName("UTF-8")));
			targetOut.close();

			logger.info("generated " + targetFile);

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
}
