package org.molgenis.ui.settings;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;

public class StaticContent extends DefaultEntity
{
	private static final long serialVersionUID = 1L;

	public static final String ENTITY_NAME = "StaticContent";

	public static final EntityMetaData META_DATA = new StaticContentMeta();

	private static final String KEY = "key_";
	private static final String CONTENT = "content";

	public StaticContent(String key, DataService dataService, String content)
	{
		super(META_DATA, dataService);
		set(KEY, key);
		set(CONTENT, content);
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