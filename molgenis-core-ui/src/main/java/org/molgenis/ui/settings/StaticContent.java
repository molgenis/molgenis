package org.molgenis.ui.settings;

import static org.molgenis.ui.settings.StaticContentMeta.CONTENT;
import static org.molgenis.ui.settings.StaticContentMeta.KEY;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

public class StaticContent extends StaticEntity
{
	public StaticContent(Entity entity)
	{
		super(entity);
	}

	public StaticContent(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public StaticContent(String key, EntityMetaData entityMeta)
	{
		super(entityMeta);
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