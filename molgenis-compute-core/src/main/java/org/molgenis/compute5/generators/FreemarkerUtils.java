package org.molgenis.compute5.generators;

import java.io.*;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class FreemarkerUtils
{
	public static final String LIST_SIGN = "[@]";

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
		finally
		{
			out.close();
		}
	}

	public static String weaveFreemarker(String strTemplate, Hashtable<String, String> values)
	{
		Configuration cfg = new Configuration();

		Template t = null;
		StringWriter out = new StringWriter();
		try
		{
			t = new Template("protocol weaving", new StringReader(strTemplate), cfg);
			t.process(values, out);
		}
		catch (TemplateException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return out.toString();
	}

	public static String weaveWithoutFreemarker(String template, Hashtable<String, String> values)
	{
		Enumeration keys = values.keys();
		while( keys.hasMoreElements() )
		{
			String key = (String) keys.nextElement();
			String value = values.get(key);
			template = template.replace(key, value);
		}
		return template;
	}
}