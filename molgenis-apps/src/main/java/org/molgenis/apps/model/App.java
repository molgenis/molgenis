package org.molgenis.apps.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.data.system.core.FreemarkerTemplate;
import org.molgenis.file.model.FileMeta;

import static org.molgenis.apps.model.AppMetaData.*;

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

	public App(String name, EntityType entityType)
	{
		super(entityType);
		setName(name);
	}

	public String getName()
	{
		return getString(APP_NAME);
	}

	public void setName(String name)
	{
		set(APP_NAME, name);
	}

	public String getDescription()
	{
		return getString(APP_DESCRIPTION);
	}

	public void setDescription(String description)
	{
		set(APP_DESCRIPTION, description);
	}

	public FileMeta getSourceFiles()
	{
		return getEntity(RESOURCE_FILES, FileMeta.class);
	}

	public void setSourceFiles(String sourcesDirectory)
	{
		set(RESOURCE_FILES, sourcesDirectory);
	}

	public boolean isActive()
	{
		return getBoolean(ACTIVE);
	}

	public void setActive(boolean isActive)
	{
		set(ACTIVE, isActive);
	}

	public String getIconHref()
	{
		return getString(APP_ICON_URL);
	}

	public void setIconHref(String iconHref)
	{
		set(APP_ICON_URL, iconHref);
	}

	public FreemarkerTemplate getHtmlTemplate()
	{
		return getEntity(APP_INDEX_HTML, FreemarkerTemplate.class);
	}

	public void setHtmlTemplate(FreemarkerTemplate htmlTemplate)
	{
		set(APP_INDEX_HTML, htmlTemplate);
	}
}

