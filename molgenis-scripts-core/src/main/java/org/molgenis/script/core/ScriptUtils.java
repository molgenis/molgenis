package org.molgenis.script.core;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

public class ScriptUtils
{
	private ScriptUtils()
	{
	}

	/**
	 * Render a script using the given parameter values
	 */
	public static String generateScript(Script script, Map<String, Object> parameterValues)
	{
		StringWriter stringWriter = new StringWriter();
		try
		{
			Template template = new Template(null, new StringReader(script.getContent()),
					new Configuration(Configuration.VERSION_2_3_21));
			template.process(parameterValues, stringWriter);
		}
		catch (TemplateException | IOException e)
		{
			throw new GenerateScriptException(
					"Error processing parameters for script [" + script.getName() + "]. " + e.getMessage());
		}
		return stringWriter.toString();
	}
}
