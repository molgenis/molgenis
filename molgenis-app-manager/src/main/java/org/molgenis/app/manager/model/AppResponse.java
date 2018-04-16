package org.molgenis.app.manager.model;

import com.google.auto.value.AutoValue;
import org.molgenis.app.manager.meta.App;
import org.molgenis.core.gson.AutoGson;

import javax.annotation.Nullable;

@AutoValue
@AutoGson(autoValueClass = AutoValue_AppResponse.class)
public abstract class AppResponse
{
	public abstract String getId();

	public abstract String getUri();

	public abstract String getLabel();

	public abstract String getDescription();

	public abstract boolean getIsActive();

	public abstract boolean getIncludeMenuAndFooter();

	public abstract String getTemplateContent();

	@Nullable
	public abstract String getAppConfig();

	public static AppResponse create(App app)
	{
		return new AutoValue_AppResponse(app.getId(), app.getUri(), app.getLabel(), app.getDescription(),
				app.isActive(), app.includeMenuAndFooter(), app.getTemplateContent(), app.getAppConfig());
	}
}
