package org.molgenis.dataexplorer.controller;

import com.google.auto.value.AutoValue;
import org.molgenis.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_Href.class)
public abstract class Href
{
	public abstract String getHref();

	@Nullable
	public abstract String getLabel();

	public static Href create(String href, String label)
	{
		return new AutoValue_Href(href, label);
	}
}