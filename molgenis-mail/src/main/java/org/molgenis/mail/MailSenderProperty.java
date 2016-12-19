package org.molgenis.mail;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.meta.PropertyType.KEY;
import static org.molgenis.data.meta.PropertyType.VALUE;

public class MailSenderProperty extends StaticEntity
{
	public MailSenderProperty(Entity entity)
	{
		super(entity);
	}

	public MailSenderProperty(EntityType entityType)
	{
		super(entityType);
	}

	public MailSenderProperty(String id, EntityType entityType)
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
