package org.molgenis.ontology.beans;

import java.util.Iterator;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.semantic.OntologyService;

public class OntologyEntityIterable implements Iterable<Entity>
{
	private final Iterable<Entity> entities;
	private final EntityMetaData entityMetaData;
	private final SearchService searchService;
	private final OntologyService ontologyService;
	private final DataService dataService;

	public OntologyEntityIterable(Iterable<Entity> entities, EntityMetaData entityMetaData, DataService dataService,
			SearchService searchService, OntologyService ontologyService)
	{
		this.entities = entities;
		this.entityMetaData = entityMetaData;
		this.searchService = searchService;
		this.ontologyService = ontologyService;
		this.dataService = dataService;
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
				return new OntologyEntity(iterator.next(), entityMetaData, dataService, searchService, ontologyService);
			}

			@Override
			public void remove()
			{

			}
		};
	}
}