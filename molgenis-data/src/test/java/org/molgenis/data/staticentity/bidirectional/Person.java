package org.molgenis.data.staticentity.bidirectional;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.staticentity.bidirectional.person1.PersonMetaData1;
import org.molgenis.data.support.StaticEntity;

public class Person extends StaticEntity
{
	public Person(Entity entity)
	{
		super(entity);
	}

	public Person(EntityType entityType)
	{
		super(entityType);
	}

	public Person(String name, EntityType entityType)
	{
		super(entityType);
		setId(name);
	}

	public Person setId(String id)
	{
		set(PersonMetaData1.ID, id);
		return this;
	}

	public String getId()
	{
		return getString(PersonMetaData1.ID);
	}
}
