package org.molgenis.core.ui.style;

import com.google.auto.value.AutoValue;
import org.molgenis.util.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Style.class)
public abstract class Style
{
	public abstract String getName();

	public abstract boolean isRemote();

	public abstract String getLocation();

	/**
	 * Create new style.
	 * The name of the style is based off of the location string, the optional 'boostrap-' prefix and '.min' and '.css'
	 * affixes are removed from the name.
	 */
	public static Style createLocal(String location)
	{
		String name = location.replaceFirst("bootstrap-", "");
		name = name.replaceFirst(".min", "");
		name = name.replaceFirst(".css", "");
		return new AutoValue_Style(name, false, location);
	}
}
