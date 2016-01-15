package org.molgenis.data.support;

import static java.util.stream.StreamSupport.stream;

import java.util.Iterator;
import java.util.stream.Stream;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityCollection;

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
	public Stream<Entity> asStream()
	{
		return stream(entities.spliterator(), false);
	}

	@Override
	public boolean isLazy()
	{
		return false;
	}
}
