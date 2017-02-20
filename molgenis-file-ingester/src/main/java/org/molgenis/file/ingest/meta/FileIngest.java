package org.molgenis.file.ingest.meta;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import java.net.MalformedURLException;
import java.net.URL;

import static org.molgenis.file.ingest.meta.FileIngestMetaData.*;

public class FileIngest extends StaticEntity
{
	public FileIngest(Entity entity)
	{
		super(entity);
	}

	public FileIngest(EntityType entityType)
	{
		super(entityType);
	}

	public FileIngest(String id, EntityType entityType)
	{
		super(entityType);
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

	public void setTargetEntity(EntityType entityType)
	{
		set(ENTITY_META_DATA, entityType);
	}

	public String getTargetEntityName()
	{
		return getEntity(ENTITY_META_DATA, EntityType.class).getFullyQualifiedName();
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
		return active != null && active.booleanValue();
	}

	public void setFailureEmail(String failureEmail)
	{
		set(FAILURE_EMAIL, failureEmail);
	}

	public String getFailureEmail()
	{
		return getString(FAILURE_EMAIL);
	}

	public void setBucket(String bucket)
	{
		set(BUCKET, bucket);
	}

	public String getBucket()
	{
		return getString(BUCKET);
	}

	public void setKey(String key)
	{
		set(KEY, key);
	}

	public String getKey()
	{
		return getString(KEY);
	}

	public void setProfile(String profile)
	{
		set(PROFILE, profile);
	}

	public String getProfile()
	{
		return getString(PROFILE);
	}

	public void setType(String type)
	{
		set(TYPE, type);
	}

	public String getType()
	{
		return getString(TYPE);
	}
}
