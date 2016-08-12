package org.molgenis.ui.style;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Style.class)
public abstract class Style
{
	public abstract String getName();

	public abstract boolean isRemote();

	public abstract String getLocation();

	public static Style createLocal(String location)
	{
		String name = location.split("-")[1].split("\\.")[0];
		return new AutoValue_Style(name, false, location);
	}
}
