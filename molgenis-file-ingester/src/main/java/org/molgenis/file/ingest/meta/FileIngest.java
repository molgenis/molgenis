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
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.support.StaticEntity;

public class FileIngest extends StaticEntity
{
	public FileIngest(Entity entity)
	{
		super(entity);
	}

	public FileIngest(EntityMetaData entityMeta)
	{
		super(entityMeta);
	}

	public FileIngest(String id, EntityMetaData entityMeta)
	{
		super(entityMeta);
		setId(id);
	}

	public void setId(String identifier)
	{
		set(ID, identifier);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setName(String name)
	{
		set(NAME, name);
	}

	public String getName()
	{
		return getString(NAME);
	}

	public void setDescription(String description)
	{
		set(DESCRIPTION, description);
	}

	public String getDescription()
	{
		return getString(DESCRIPTION);
	}

	public void setUrl(URL url)
	{
		set(URL, url != null ? url.toString() : null);
	}

	public URL getUrl() throws MalformedURLException
	{
		return new URL(getString(URL));
	}

	public void setLoader(String loader)
	{
		set(LOADER, loader);
	}

	public String getLoader()
	{
		return getString(LOADER);
	}

	public void setTargetEntity(EntityMetaData entityMetaData)
	{
		set(ENTITY_META_DATA, entityMetaData);
	}

	public String getTargetEntityName()
	{
		return getEntity(ENTITY_META_DATA, EntityMetaData.class).getName();
	}

	public void setCronExpression(String cronExpression)
	{
		set(CRONEXPRESSION, cronExpression);
	}

	public String getCronExpression()
	{
		return getString(CRONEXPRESSION);
	}

	public void setActive(boolean active)
	{
		set(ACTIVE, active);
	}

	public boolean isActive()
	{
		Boolean active = getBoolean(ACTIVE);
		return active != null ? active.booleanValue() : false;
	}

	public void setFailureEmail(String failureEmail)
	{
		set(FAILURE_EMAIL, failureEmail);
	}

	public String getFailureEmail()
	{
		return getString(FAILURE_EMAIL);
	}
}
