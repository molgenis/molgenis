package org.molgenis.file;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.OwnedEntityMetaData;

public class FileMeta extends DefaultEntity
{
	private static final long serialVersionUID = 1L;

	public static final String ENTITY_NAME = "FileMeta";
	public static final EntityMetaData META_DATA = new FileMetaMetaData();

	public static final String ID = "id";
	public static final String FILENAME = "filename";
	public static final String CONTENT_TYPE = "contentType";
	public static final String SIZE = "size";
	public static final String URL = "url";

	public FileMeta(DataService dataService)
	{
		super(META_DATA, dataService);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getFilename()
	{
		return getString(FILENAME);
	}

	public void setFilename(String filename)
	{
		set(FILENAME, filename);
	}

	public String getContentType()
	{
		return getString(CONTENT_TYPE);
	}

	public void setContentType(String contentType)
	{
		set(CONTENT_TYPE, contentType);
	}

	public Long getSize()
	{
		return getLong(SIZE);
	}

	public void setSize(Long size)
	{
		set(SIZE, size);
	}

	public String getUrl()
	{
		return getString(URL);
	}

	public void setUrl(String url)
	{
		set(URL, url);
	}

	public String getOwnerUsername()
	{
		return getString(OwnedEntityMetaData.ATTR_OWNER_USERNAME);
	}

	@Override
	public String getIdValue()
	{
		return getString(ID);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return META_DATA;
	}
}
