package org.molgenis.file.ingest.meta;

import static org.molgenis.file.ingest.meta.FileIngestMetaData.ACTIVE;
import static org.molgenis.file.ingest.meta.FileIngestMetaData.CRONEXPRESSION;
import static org.molgenis.file.ingest.meta.FileIngestMetaData.DESCRIPTION;
import static org.molgenis.file.ingest.meta.FileIngestMetaData.ENTITY_META_DATA;
import static org.molgenis.file.ingest.meta.FileIngestMetaData.FAILURE_EMAIL;
import static org.molgenis.file.ingest.meta.FileIngestMetaData.ID;
import static org.molgenis.file.ingest.meta.FileIngestMetaData.LOADER;
import static org.molgenis.file.ingest.meta.FileIngestMetaData.NAME;
import static org.molgenis.file.ingest.meta.FileIngestMetaData.URL;

import java.net.MalformedURLException;
import java.net.URL;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.SystemEntity;

public class FileIngest extends SystemEntity
{
	public FileIngest(Entity entity)
	{
		super(entity);
	}

	public FileIngest(FileIngestMetaData fileIngestMetaData)
	{
		super(fileIngestMetaData);
	}

	public FileIngest(String id, FileIngestMetaData fileIngestMetaData)
	{
		super(fileIngestMetaData);
		setIdentifier(id);
	}

	private void setIdentifier(String identifier)
	{
		set(ID, identifier);
	}

	public String getIdentifier()
	{
		return getString(ID);
	}

	public String getName()
	{
		return getString(NAME);
	}

	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	public URL getUrl() throws MalformedURLException
	{
		return new URL(getString(URL));
	}

	public String getLoader()
	{
		return getString(LOADER);
	}

	public String getTargetEntityName()
	{
		return getEntity(ENTITY_META_DATA, FileIngestMetaData.class).getName();
	}

	public String getCronExpression()
	{
		return getString(CRONEXPRESSION);
	}

	public boolean isActive()
	{
		return getBoolean(ACTIVE);
	}

	public String getFailureEmail()
	{
		return getString(FAILURE_EMAIL);
	}
}
