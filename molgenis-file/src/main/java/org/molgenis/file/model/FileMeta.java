package org.molgenis.file.model;

import static org.molgenis.file.model.FileMetaMetaData.CONTENT_TYPE;
import static org.molgenis.file.model.FileMetaMetaData.FILENAME;
import static org.molgenis.file.model.FileMetaMetaData.ID;
import static org.molgenis.file.model.FileMetaMetaData.SIZE;
import static org.molgenis.file.model.FileMetaMetaData.URL;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.security.owned.OwnedEntity;

public class FileMeta extends OwnedEntity
{
	public FileMeta(Entity entity)
	{
		super(entity);
	}

	public FileMeta(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public FileMeta(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setId(id);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setFilename(String filename)
	{
		set(FILENAME, filename);
	}

	public String getFilename()
	{
		return getString(FILENAME);
	}

	public void setContentType(String contentType)
	{
		set(CONTENT_TYPE, contentType);
	}

	public String getContentType()
	{
		return getString(CONTENT_TYPE);
	}

	public void setSize(Long size)
	{
		set(SIZE, size);
	}

	public Long getSize()
	{
		return getLong(SIZE);
	}

	public void setUrl(String url)
	{
		set(URL, url);
	}

	public String getUrl()
	{
		return getString(URL);
	}
}
