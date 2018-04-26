package org.molgenis.data.staticentity.bidirectional;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.staticentity.bidirectional.authorbook1.AuthorMetaData1;
import org.molgenis.data.support.StaticEntity;

public class Author extends StaticEntity
{
	public Author(Entity entity)
	{
		super(entity);
	}

	public Author(EntityType entityType)
	{
		super(entityType);
	}

	public Author(String name, EntityType entityType)
	{
		super(entityType);
		setId(name);
	}

	public Author setId(String id)
	{
		set(AuthorMetaData1.ID, id);
		return this;
	}

	public String getId()
	{
		return getString(AuthorMetaData1.ID);
	}
}
