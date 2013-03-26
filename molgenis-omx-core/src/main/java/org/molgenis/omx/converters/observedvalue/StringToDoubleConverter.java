package org.molgenis.omx.converters.observedvalue;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.observ.ObservableFeature;

public class StringToDoubleConverter implements StringConverter<Double>
{

	@Override
	public Double fromString(String value, Database db, ObservableFeature feature)
	{
		if (value == null)
		{
			return null;
		}

		return Double.parseDouble(value);
	}

}
