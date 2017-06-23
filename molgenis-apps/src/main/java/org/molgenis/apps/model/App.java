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

	public String getId()
	{
		return getString(ID);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getName()
	{
		return getString(NAME);
	}

	public void setName(String name)
	{
		set(NAME, name);
	}

	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	public void setDescription(String description)
	{
		set(DESCRIPTION, description);
	}

	public FileMeta getSourceFiles()
	{
		return getEntity(RESOURCE_ZIP, FileMeta.class);
	}

	public void setSourceFiles(String sourcesDirectory)
	{
		set(RESOURCE_ZIP, sourcesDirectory);
	}

	public boolean isActive()
	{
		return getBoolean(IS_ACTIVE);
	}

	public void setActive(boolean isActive)
	{
		set(IS_ACTIVE, isActive);
	}

	public String getIconHref()
	{
		return getString(ICON_HREF);
	}

	public void setIconHref(String iconHref)
	{
		set(ICON_HREF, iconHref);
	}

	public Boolean getUseFreemarkerTemplate()
	{
		return getBoolean(USE_FREEMARKER_TEMPLATE);
	}

	public void setUseFreemarkerTemplate(boolean useFreemarkerTemplate)
	{
		set(USE_FREEMARKER_TEMPLATE, useFreemarkerTemplate);
	}

	public FreemarkerTemplate getHtmlTemplate()
	{
		return getEntity(LANDING_PAGE_HTML_TEMPLATE, FreemarkerTemplate.class);
	}

	public void setHtmlTemplate(FreemarkerTemplate htmlTemplate)
	{
		if (this.getUseFreemarkerTemplate())
		{
			set(LANDING_PAGE_HTML_TEMPLATE, htmlTemplate);
		}
		else
		{
			//TODO: implement gethtml?
		}

	}
}

