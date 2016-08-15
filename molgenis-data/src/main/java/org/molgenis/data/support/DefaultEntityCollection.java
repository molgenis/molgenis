package org.molgenis.data.support;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityCollection;

import java.util.Iterator;

public class DefaultEntityCollection implements EntityCollection
{
	private final Iterable<String> attrNames;
	private final Iterable<Entity> entities;

	public DefaultEntityCollection(Iterable<Entity> entities, Iterable<String> attrNames)
	{
		this.entities = entities;
		this.attrNames = attrNames;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return entities.iterator();
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		return attrNames;
	}

	@Override
	public boolean isLazy()
	{
		return false;
	}
}
