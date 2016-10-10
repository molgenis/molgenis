package org.molgenis.ui.settings;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.ui.settings.StaticContentMeta.CONTENT;
import static org.molgenis.ui.settings.StaticContentMeta.KEY;

public class StaticContent extends StaticEntity
{
	public StaticContent(Entity entity)
	{
		super(entity);
	}

	public StaticContent(EntityType entityType)
	{
		super(entityType);
	}

	public StaticContent(String key, EntityType entityType)
	{
		super(entityType);
		setKey(key);
	}

	public String getKey()
	{
		return getString(KEY);
	}

	private void setKey(String key)
	{
		set(KEY, key);
	}

	public String getContent()
	{
		return getString(CONTENT);
	}

	public void setContent(String content)
	{
		set(CONTENT, content);
	}
}