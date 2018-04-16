package org.molgenis.app.manager.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import javax.annotation.Nullable;

import static org.molgenis.app.manager.meta.AppMetadata.*;

public class App extends StaticEntity
{
	public App(Entity entity)
	{
		super(entity);
	}

	public App(EntityType entityType)
	{
		super(entityType);
	}

	public App(String label, EntityType entityType)
	{
		super(entityType);
		setLabel(label);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getUri()
	{
		return getString(URI);
	}

	public void setUri(String uri)
	{
		set(URI, uri);
	}

	public String getLabel()
	{
		return getString(LABEL);
	}

	public void setLabel(String label)
	{
		set(LABEL, label);
	}

	@Nullable
	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	public void setDescription(String description)
	{
		set(DESCRIPTION, description);
	}

	public boolean isActive()
	{
		return getBoolean(IS_ACTIVE);
	}

	public void setActive(boolean isActive)
	{
		set(IS_ACTIVE, isActive);
	}

	public boolean includeMenuAndFooter()
	{
		return getBoolean(INCLUDE_MENU_AND_FOOTER);
	}

	public void setIncludeMenuAndFooter(boolean includeMenuAndFooter)
	{
		set(INCLUDE_MENU_AND_FOOTER, includeMenuAndFooter);
	}

	public String getTemplateContent()
	{
		return getString(TEMPLATE_CONTENT);
	}

	public void setTemplateContent(String templateContent)
	{
		set(TEMPLATE_CONTENT, templateContent);
	}

	public String getAppVersion()
	{
		return getString(APP_VERSION);
	}

	public void setAppVersion(String appVersion)
	{
		set(APP_VERSION, appVersion);
	}

	public String getApiDependency()
	{
		return getString(API_DEPENDENCY);
	}

	public void setApiDependency(String apiDependency)
	{
		set(API_DEPENDENCY, apiDependency);
	}

	public String getAppConfig()
	{
		return getString(APP_CONFIG);
	}

	public void setAppConfig(String appConfig)
	{
		set(APP_CONFIG, appConfig);
	}
}

