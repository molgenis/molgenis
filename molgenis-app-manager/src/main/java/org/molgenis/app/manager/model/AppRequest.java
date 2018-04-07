package org.molgenis.app.manager.model;

import com.google.auto.value.AutoValue;
import org.molgenis.core.gson.AutoGson;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AppRequest.class)
public abstract class AppRequest
{
	public abstract String getId();

	public abstract String getLabel();

	public abstract boolean getIncludeMenuAndFooter();

	public static AppRequest create(String id, String label, boolean includeMenuAndFooter)
	{
		return new AutoValue_AppRequest(id, label, includeMenuAndFooter);
	}
}
