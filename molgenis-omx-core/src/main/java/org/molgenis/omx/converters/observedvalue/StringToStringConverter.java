package org.molgenis.omx.converters.observedvalue;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.observ.ObservableFeature;

public class StringToStringConverter implements StringConverter<String>
{
	@Override
	public String fromString(String value, Database db, ObservableFeature feature)
	{
		return value;
	}

}
