package org.molgenis.data.system.core;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

import static org.molgenis.data.system.core.FreemarkerTemplateMetaData.*;

public class FreemarkerTemplate extends StaticEntity
{
	public FreemarkerTemplate(Entity entity)
	{
		super(entity);
	}

	public FreemarkerTemplate(EntityType entityType)
	{
		super(entityType);
	}

	public FreemarkerTemplate(String id, EntityType entityType)
	{
		super(entityType);
		setId(id);
	}

	public String getId()
	{
		return getString(ID);
	}

	public void setId(String id)
	{
		set(ID, id);
	}

	public String getName()
	{
		return getString(NAME);
	}

	public void setName(String name)
	{
		set(NAME, name);
	}

	public String getValue()
	{
		return getString(VALUE);
	}

	public void setValue(String value)
	{
		set(VALUE, value);
	}
}
