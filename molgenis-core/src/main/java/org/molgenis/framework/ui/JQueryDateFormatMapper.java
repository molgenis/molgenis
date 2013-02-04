package org.molgenis.framework.ui;

import java.util.HashMap;
import java.util.Map;

public class JQueryDateFormatMapper
{
	private static Map<String, String> jQueryToJavaDateFormatMap;
	private static Map<String, String> javaToJQueryDateFormatMap;

	static
	{
		jQueryToJavaDateFormatMap = new HashMap<String, String>();
		jQueryToJavaDateFormatMap.put("dd-mm-yy", "dd-MM-yyyy");
		jQueryToJavaDateFormatMap.put("yy-mm-dd", "yyyy-MM-dd");

		javaToJQueryDateFormatMap = new HashMap<String, String>();
		for (Map.Entry<String, String> entry : jQueryToJavaDateFormatMap.entrySet())
			javaToJQueryDateFormatMap.put(entry.getValue(), entry.getKey());
	}

	public static String toJQueryDateFormat(String javaDateFormat)
	{
		return toJQueryDateFormat(javaDateFormat, null);
	}

	public static String toJQueryDateFormat(String javaDateFormat, String defaultDateFormat)
	{
		String dateFormat = javaToJQueryDateFormatMap.get(javaDateFormat);
		return dateFormat != null ? dateFormat : defaultDateFormat;
	}

	public static String toJavaDateFormat(String jQueryDateFormat)
	{
		return toJavaDateFormat(jQueryDateFormat, null);
	}

	public static String toJavaDateFormat(String jQueryDateFormat, String defaultDateFormat)
	{
		return jQueryToJavaDateFormatMap.get(jQueryDateFormat);
	}
}
