package org.molgenis.omx.converters.observedvalue;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.observ.ObservableFeature;

public class StringToIntConverter implements StringConverter<Integer>
{
	@Override
	public Integer fromString(String value, Database db, ObservableFeature feature)
	{
		if (value == null)
		{
			return null;
		}

		return Integer.parseInt(value);
	}

}
