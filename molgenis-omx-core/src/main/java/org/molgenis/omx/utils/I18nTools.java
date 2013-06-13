package org.molgenis.omx.utils;

import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

/**
 * Reads internationalized strings of the form {"en":"car", "nl":"auto"}
 * 
 * @author Dennis
 */
public class I18nTools
{
	private static final Logger logger = Logger.getLogger(I18nTools.class);

	private static final String DEFAULT_LANG = "en";

	private I18nTools()
	{
	}

	/**
	 * If the given string is a internationalized string return the value for the default language, else return the
	 * input string
	 * 
	 * @param i18nStr
	 *            internationalized string of the form {"en":"car", "nl":"auto"} or a plain string
	 * @return
	 */
	public static String get(String i18nStr)
	{
		return get(i18nStr, DEFAULT_LANG);
	}

	/**
	 * If the given string is a internationalized string return the value for the given languange, else return the input
	 * string
	 * 
	 * @param i18nStr
	 *            internationalized string of the form {"en":"car", "nl":"auto"} or a plain string
	 * @return
	 */
	public static String get(String i18nStr, String lang)
	{
		String str = i18nStr;
		if (i18nStr != null && i18nStr.startsWith("{") && i18nStr.endsWith("}"))
		{
			try
			{
				Map<String, String> i18nMap = new Gson().fromJson(i18nStr, new TypeToken<Map<String, String>>()
				{
				}.getType());
				str = i18nMap.get(lang);
			}
			catch (JsonParseException e)
			{
				logger.warn(e);
				str = i18nStr;
			}
		}
		return str;
	}
}
