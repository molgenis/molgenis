package org.molgenis.compute5.generators;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;

public class FreemarkerUtils
{
	public void applyTemplate(Map<String, Object> model, String templatePath, File dotFile) throws IOException
	{
		Configuration conf = new Configuration();
		conf.setClassForTemplateLoading(this.getClass(), "");
		conf.setObjectWrapper(new DefaultObjectWrapper());
	
		Template template = conf.getTemplate(templatePath);
		dotFile.getParentFile().mkdirs();
		FileWriter out = new FileWriter(dotFile);
		try
		{
			template.process(model, out);
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
	}

}