package org.molgenis.file.ingest.meta;

import java.net.MalformedURLException;
import java.net.URL;

import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.EntityMetaDataMetaData;
import org.molgenis.data.support.DefaultEntity;

public class FileIngest extends DefaultEntity
{
	private static final long serialVersionUID = 1L;
	public static final EntityMetaData META_DATA = new FileIngestMetaData();

	public FileIngest(DataService dataService)
	{
		super(META_DATA, dataService);
	}

	public String getIdentifier()
	{
		return getString(FileIngestMetaData.ID);
	}

	public String getName()
	{
		return getString(FileIngestMetaData.NAME);
	}

	public String getDescription()
	{
		return getString(FileIngestMetaData.DESCRIPTION);
	}

	public URL getUrl() throws MalformedURLException
	{
		return new URL(getString(FileIngestMetaData.URL));
	}

	public String getLoader()
	{
		return getString(FileIngestMetaData.LOADER);
	}

	public String getTargetEntityName()
	{
		return getEntity(FileIngestMetaData.ENTITY_META_DATA).getString(EntityMetaDataMetaData.FULL_NAME);
	}

	public String getCronExpression()
	{
		return getString(FileIngestMetaData.CRONEXPRESSION);
	}

	public boolean isActive()
	{
		return getBoolean(FileIngestMetaData.ACTIVE);
	}

	public String getFailureEmail()
	{
		return getString(FileIngestMetaData.FAILURE_EMAIL);
	}
}
