package org.molgenis.ui.style;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.file.model.FileMeta;

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
		return getString(StyleMetadata.ID);
	}

	public void setId(String id)
	{
		set(StyleMetadata.ID, id);
	}

	public String getName()
	{
		return getString(StyleMetadata.NAME);
	}

	public void setName(String name)
	{
		set(StyleMetadata.NAME, name);
	}

	public FileMeta getBootstrap3Theme()
	{
		return getEntity(StyleMetadata.BOOTSTRAP_3_THEME, FileMeta.class);
	}

	public void setBootstrap3Theme(FileMeta bootstrap3FileMeta)
	{
		set(StyleMetadata.BOOTSTRAP_3_THEME, bootstrap3FileMeta);
	}

	public FileMeta getBootstrap4Theme()
	{
		return getEntity(StyleMetadata.BOOTSTRAP_4_THEME, FileMeta.class);
	}

	public void setBootstrap4Theme(FileMeta bootstrap4FileMeta)
	{
		set(StyleMetadata.BOOTSTRAP_4_THEME, bootstrap4FileMeta);
	}
}
