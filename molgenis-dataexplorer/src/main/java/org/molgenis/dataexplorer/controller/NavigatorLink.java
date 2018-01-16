package org.molgenis.dataexplorer.controller;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_NavigatorLink.class)
public abstract class NavigatorLink
{
	public abstract String getHref();

	@Nullable
	public abstract String getLabel();

	public static NavigatorLink create(String href, String label)
	{
		return new AutoValue_NavigatorLink(href, label);
	}
}