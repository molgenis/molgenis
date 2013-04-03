package org.molgenis.omx.converters.observedvalue;

import java.util.HashMap;
import java.util.Map;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.observ.ObservableFeature;

/**
 * Converts the value of an ObservedValue from string to a java type according to the datatype of the ObservableFeature
 * 
 * @author erwin
 * 
 */
public class ValueConverter
{
	private static Map<String, StringConverter<?>> converters = new HashMap<String, StringConverter<?>>();
	static
	{
		// support all feature data types as stated in the data model:
		// [xref,string,categorical,nominal,ordinal,date,datetime,int,code,image,decimal,bool,file,log,data,exe]
		converters.put("xref", new StringToStringConverter());
		converters.put("string", new StringToStringConverter());
		converters.put("categorical", new StringToCategoricalConverter());
		converters.put("nominal", new StringToStringConverter());
		converters.put("ordinal", new StringToStringConverter());
		converters.put("date", new StringToStringConverter());
		converters.put("datetime", new StringToStringConverter());
		converters.put("int", new StringToIntConverter());
		converters.put("code", new StringToStringConverter());
		converters.put("image", new StringToStringConverter());
		converters.put("decimal", new StringToDoubleConverter());
		converters.put("bool", new StringToBooleanConverter());
		converters.put("file", new StringToStringConverter());
		converters.put("log", new StringToStringConverter());
		converters.put("data", new StringToStringConverter());
		converters.put("exe", new StringToStringConverter());
	}

	public static Object fromString(String value, Database db, ObservableFeature feature)
	{
		StringConverter<?> converter = converters.get(feature.getDataType());
		if (converter == null)
		{
			throw new IllegalArgumentException("Unsupported dataType [" + feature.getDataType() + "]");
		}

		return converter.fromString(value, db, feature);
	}
}
