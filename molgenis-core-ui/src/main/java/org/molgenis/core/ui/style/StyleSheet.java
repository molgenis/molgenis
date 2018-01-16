package org.molgenis.core.ui.style;

import org.molgenis.data.Entity;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class StyleSheet extends StaticEntity
{
	public StyleSheet(Entity entity)
	{
		super(entity);
	}

	public StyleSheet(EntityType entityType)
	{
		super(entityType);
	}

	public StyleSheet(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public String getId()
	{
		return getString(StyleSheetMetadata.ID);
	}

	public void setId(String id)
	{
		set(StyleSheetMetadata.ID, id);
	}

	public String getName()
	{
		return getString(StyleSheetMetadata.NAME);
	}

	public void setName(String name)
	{
		set(StyleSheetMetadata.NAME, name);
	}

	public FileMeta getBootstrap3Theme()
	{
		return getEntity(StyleSheetMetadata.BOOTSTRAP_3_THEME, FileMeta.class);
	}

	public void setBootstrap3Theme(FileMeta bootstrap3FileMeta)
	{
		set(StyleSheetMetadata.BOOTSTRAP_3_THEME, bootstrap3FileMeta);
	}

	public FileMeta getBootstrap4Theme()
	{
		return getEntity(StyleSheetMetadata.BOOTSTRAP_4_THEME, FileMeta.class);
	}

	public void setBootstrap4Theme(FileMeta bootstrap4FileMeta)
	{
		set(StyleSheetMetadata.BOOTSTRAP_4_THEME, bootstrap4FileMeta);
	}
}
