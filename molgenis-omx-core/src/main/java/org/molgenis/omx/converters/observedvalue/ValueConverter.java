package org.molgenis.omx.converters.observedvalue;

import java.util.HashMap;
import java.util.Map;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.observ.ObservableFeature;

/**
 * Converts the value of an ObservedValue from string to a java type according
 * to the datatype of the ObservableFeature
 * 
 * @author erwin
 * 
 */
public class ValueConverter
{
	private static Map<String, StringConverter<?>> converters = new HashMap<String, StringConverter<?>>();
	static
	{
		converters.put("string", new StringToStringConverter());
		converters.put("categorical", new StringToCategoricalConverter());
		converters.put("int", new StringToIntConverter());
		converters.put("integer", new StringToIntConverter());
		converters.put("decimal", new StringToDoubleConverter());
		converters.put("bool", new StringToBooleanConverter());
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
