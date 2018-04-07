package org.molgenis.app.manager.meta;

import org.molgenis.core.ui.data.system.core.FreemarkerTemplate;
import org.molgenis.data.Entity;
import org.molgenis.data.file.model.FileMeta;
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

	@Nullable
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

	public Boolean getUseFreemarkerTemplate()
	{
		return getBoolean(USE_FREEMARKER_TEMPLATE);
	}

	public void setUseFreemarkerTemplate(boolean useFreemarkerTemplate)
	{
		set(USE_FREEMARKER_TEMPLATE, useFreemarkerTemplate);
	}

	@Nullable
	public FreemarkerTemplate getHtmlTemplate()
	{
		return getEntity(LANDING_PAGE_HTML_TEMPLATE, FreemarkerTemplate.class);
	}

	public void setHtmlTemplate(FreemarkerTemplate htmlTemplate)
	{
		set(LANDING_PAGE_HTML_TEMPLATE, htmlTemplate);
	}
}

