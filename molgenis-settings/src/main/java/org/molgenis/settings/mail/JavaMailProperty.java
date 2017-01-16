package org.molgenis.settings.mail;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.settings.PropertyType.KEY;
import static org.molgenis.settings.PropertyType.VALUE;

public class JavaMailProperty extends StaticEntity
{
	public JavaMailProperty(Entity entity)
	{
		super(entity);
	}

	public JavaMailProperty(EntityType entityType)
	{
		super(entityType);
	}

	public JavaMailProperty(String id, EntityType entityType)
	{
		super(entityType);
		setKey(id);
	}

	public String getKey()
	{
		return getString(KEY);
	}

	public void setKey(String key)
	{
		set(KEY, key);
	}

	public String getValue()
	{
		return getString(VALUE);
	}

	public void setValue(String name)
	{
		set(VALUE, name);
	}
}
