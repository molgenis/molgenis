package org.molgenis.ui.settings;

import static org.molgenis.ui.settings.StaticContentMeta.CONTENT;
import static org.molgenis.ui.settings.StaticContentMeta.KEY;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntity;

public class StaticContent extends SystemEntity
{
	public StaticContent(Entity entity)
	{
		super(entity);
	}

	public StaticContent(StaticContentMeta staticContentMeta)
	{
		super(staticContentMeta);
	}

	public StaticContent(String key, StaticContentMeta staticContentMeta)
	{
		super(staticContentMeta);
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