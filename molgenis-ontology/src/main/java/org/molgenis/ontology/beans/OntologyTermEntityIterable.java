package org.molgenis.ontology.beans;

import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.elasticsearch.SearchService;

public class OntologyTermEntityIterable implements Iterable<Entity>
{
	private final Iterable<Entity> entities;
	private final EntityMetaData entityMetaData;
	private final SearchService searchService;

	public OntologyTermEntityIterable(Iterable<Entity> entities, EntityMetaData entityMetaData,
			SearchService searchService)
	{
		this.entities = entities;
		this.entityMetaData = entityMetaData;
		this.searchService = searchService;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		final Iterator<Entity> iterator = entities.iterator();
		return new Iterator<Entity>()
		{
			@Override
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			@Override
			public Entity next()
			{
				return new OntologyTermEntity(iterator.next(), entityMetaData, searchService);
			}

			@Override
			public void remove()
			{

			}
		};
	}
}