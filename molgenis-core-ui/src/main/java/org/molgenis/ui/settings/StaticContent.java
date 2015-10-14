package org.molgenis.ui.settings;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;

public class StaticContent extends DefaultEntity
{
	private static final long serialVersionUID = 1L;

	public static final String ENTITY_NAME = "StaticContent";

	public static final EntityMetaData META_DATA = new StaticContentMeta();

	static final String KEY = "key_";
	static final String CONTENT = "content";

	public StaticContent(DataService dataService)
	{
		super(META_DATA, dataService);
	}

	public StaticContent(String key, DataService dataService)
	{
		super(META_DATA, dataService);
		set(KEY, key);
	}

	public String getKey()
	{
		return getString(KEY);
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