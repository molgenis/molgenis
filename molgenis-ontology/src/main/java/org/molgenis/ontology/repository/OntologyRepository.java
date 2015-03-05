package org.molgenis.ontology.repository;

import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.ontology.model.OntologyMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class OntologyRepository implements Repository
{
	private final OntologyLoader ontologyLoader;
	private final String ontologyId;

	public OntologyRepository(OntologyLoader ontologyLoader, UuidGenerator uuidGenerator)
			throws OWLOntologyCreationException
	{
		if (ontologyLoader == null) throw new IllegalArgumentException("OntologyLoader is null!");
		if (uuidGenerator == null) throw new IllegalArgumentException("UuidGenerator is null!");
		this.ontologyLoader = ontologyLoader;
		ontologyId = uuidGenerator.generateId();
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
				entity.set(OntologyMetaData.ID, ontologyId);
				entity.set(OntologyMetaData.ONTOLOGY_IRI, ontologyLoader.getOntologyIRI());
				entity.set(OntologyMetaData.ONTOLOGY_NAME, ontologyLoader.getOntologyName());
				return entity;
			}
		};
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

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count()
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Query query()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long count(Query q)
	{
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity findOne(Query q)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entity findOne(Object id)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Object> ids)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AggregateResult aggregate(AggregateQuery aggregateQuery)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Entity entity)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void update(Iterable<? extends Entity> records)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Entity entity)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void delete(Iterable<? extends Entity> entities)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteById(Object id)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteById(Iterable<Object> ids)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void deleteAll()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void add(Entity entity)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Integer add(Iterable<? extends Entity> entities)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void flush()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void clearCache()
	{
		// TODO Auto-generated method stub

	}
}