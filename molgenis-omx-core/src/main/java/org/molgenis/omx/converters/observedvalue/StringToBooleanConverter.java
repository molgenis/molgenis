package org.molgenis.omx.converters.observedvalue;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.observ.ObservableFeature;

public class StringToBooleanConverter implements StringConverter<Boolean>
{

	@Override
	public Boolean fromString(String value, Database db, ObservableFeature feature)
	{
		if (value == null)
		{
			return null;
		}

		return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("y") || value.equalsIgnoreCase("yes")
				|| value.equalsIgnoreCase("j") || value.equalsIgnoreCase("ja") || value.equalsIgnoreCase("1");
	}

}
