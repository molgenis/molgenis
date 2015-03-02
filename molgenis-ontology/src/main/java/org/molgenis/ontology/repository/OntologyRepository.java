package org.molgenis.ontology.repository;

import java.io.IOException;
import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.ontology.model.OntologyMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class OntologyRepository implements Repository
{
	private final OntologyLoader ontologyLoader;
	private final UuidGenerator uuidGenerator;

	public OntologyRepository(OntologyLoader ontologyLoader, UuidGenerator uuidGenerator)
			throws OWLOntologyCreationException
	{
		if (ontologyLoader == null) throw new IllegalArgumentException("OntologyLoader is null!");
		if (uuidGenerator == null) throw new IllegalArgumentException("UuidGenerator is null!");
		this.ontologyLoader = ontologyLoader;
		this.uuidGenerator = uuidGenerator;
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
				if (count == 0)
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
				entity.set(OntologyMetaData.ID, uuidGenerator.generateId());
				entity.set(OntologyMetaData.ONTOLOGY_IRI, ontologyLoader.getOntologyIRI());
				entity.set(OntologyMetaData.ONTOLOGY_NAME, ontologyLoader.getOntologyName());
				return entity;
			}
		};
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

	@Override
	public String getName()
	{
		return OntologyMetaData.ENTITY_NAME;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return OntologyMetaData.getEntityMetaData();
	}

	@Override
	public void close() throws IOException
	{
		// TODO Auto-generated method stub
	}
}