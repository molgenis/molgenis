package org.molgenis.data.support;

import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityCollection;

public class DefaultEntityCollection implements EntityCollection
{
	private final Iterable<String> entityNames;
	private final Iterable<Entity> entities;

	public DefaultEntityCollection(Iterable<Entity> entities, Iterable<String> entityNames)
	{
		this.entities = entities;
		this.entityNames = entityNames;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return entities.iterator();
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return entityNames;
	}

}
