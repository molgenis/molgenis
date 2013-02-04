package org.molgenis.framework.ui.html;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.molgenis.util.tuple.KeyValueTuple;
import org.molgenis.util.tuple.WritableTuple;

import freemarker.core.Environment;
import freemarker.template.Configuration;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateModel;

/**
 * WidgetFactory is a helper class to create input widgets such as date, action,
 * ints etc. It is used by WidgetFactory.ftl to easily create widgets in you
 * ftl. For example in ftl: <@date name="mydate"/>
 */
public class WidgetFactory
{
	public static class HtmlInputAdapter implements TemplateDirectiveModel
	{
		HtmlInput<?> input = null;

		private HtmlInputAdapter(HtmlInput<?> input)
		{
			this.input = input;
		}

		@SuppressWarnings(
		{ "rawtypes" })
		@Override
		public void execute(Environment env, Map params, TemplateModel[] loopVars, TemplateDirectiveBody body)
		{
			// transform params
			WritableTuple tuple = new KeyValueTuple();
			for (Object key : params.keySet())
			{
				if (params.get(key) instanceof SimpleScalar)
				{
					tuple.set(key.toString(), ((SimpleScalar) params.get(key)).toString());
				}
				else
				{
					tuple.set(key.toString(), params.get(key));
				}
			}

			// try to get html
			try
			{
				env.getOut().write(input.render(tuple));
			}
			catch (Exception e)
			{
				try
				{
					env.getOut().write("macro failed: " + e.getMessage() + "<br/>");
				}
				catch (IOException e1)
				{
					e1.printStackTrace();
				}
				e.printStackTrace();
			}
		}
	}

	public static void configure(Configuration conf)
	{
		Map<String, HtmlInput<?>> map = new LinkedHashMap<String, HtmlInput<?>>();
		map.put("action", new ActionInput());
		map.put("bool", new BoolInput());
		map.put("string", new StringInput());
		map.put("checkbox", new CheckboxInput());
		map.put("date", new DateInput());
		map.put("datetime", new DateInput());
		map.put("decimal", new DecimalInput());
		map.put("enum", new EnumInput());
		map.put("file", new FileInput());
		map.put("int", new IntInput());
		map.put("xref", new XrefInput());
		map.put("mref", new MrefInput());
		map.put("text", new TextInput());
		map.put("hidden", new HiddenInput());

		for (Entry<String, HtmlInput<?>> entry : map.entrySet())
		{
			conf.setSharedVariable(entry.getKey(), new HtmlInputAdapter(entry.getValue()));
		}

	}
}
