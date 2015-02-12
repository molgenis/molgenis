package org.molgenis.ontology.repository;

import java.util.Iterator;

import org.molgenis.data.Countable;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.SearchService;
import org.molgenis.data.elasticsearch.util.MapperTypeSanitizer;
import org.molgenis.data.support.MapEntity;
import org.molgenis.ontology.utils.OntologyLoader;

public class OntologyIndexRepository extends AbstractOntologyRepository implements Countable
{
	private final OntologyLoader ontologyLoader;
	public final static String TYPE_ONTOLOGY = "indexedOntology";

	public OntologyIndexRepository(OntologyLoader loader, String name, SearchService searchService)
	{
		super(name, searchService);
		if (loader == null) throw new IllegalArgumentException("OntologyLoader is null!");
		ontologyLoader = loader;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new Iterator<Entity>()
		{
			private int count = 0;

			@Override
			public boolean hasNext()
			{
				if (count < count())
				{
					count++;
					return true;
				}
				return false;
			}

			@Override
			public Entity next()
			{
				Entity entity = new MapEntity();
				entity.set(ID, MapperTypeSanitizer.sanitizeMapperType(ontologyLoader.getOntologyName()));
				entity.set(ONTOLOGY_IRI, ontologyLoader.getOntologyIRI());
				entity.set(ONTOLOGY_NAME, ontologyLoader.getOntologyName());
				entity.set(ENTITY_TYPE, TYPE_ONTOLOGY);
				return entity;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

		};
	}

	public long count()
	{
		return 1;
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getUrl()
	{
		throw new UnsupportedOperationException();
	}
}
