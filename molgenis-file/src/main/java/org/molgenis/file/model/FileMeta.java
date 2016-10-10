package org.molgenis.file.model;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.security.owned.OwnedEntity;

import static org.molgenis.file.model.FileMetaMetaData.*;

public class FileMeta extends OwnedEntity
{
	public FileMeta(Entity entity)
	{
		super(entity);
	}

	public FileMeta(EntityType entityType)
	{
		super(entityType);
	}

	public FileMeta(String id, EntityType entityType)
	{
		super(entityType);
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
