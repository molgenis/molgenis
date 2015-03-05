package org.molgenis.ontology.repository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
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
import org.molgenis.ontology.model.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.semanticweb.owlapi.model.OWLClass;

public class OntologyTermDynamicAnnotationRepository implements Repository
{
	private final OntologyLoader ontologyLoader;
	private final UuidGenerator uuidGenerator;
	private final Map<String, Map<String, String>> referenceIds = new HashMap<String, Map<String, String>>();

	public OntologyTermDynamicAnnotationRepository(OntologyLoader ontologyLoader, UuidGenerator uuidGenerator)
	{
		this.ontologyLoader = ontologyLoader;
		this.uuidGenerator = uuidGenerator;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new Iterator<Entity>()
		{
			final Iterator<OWLClass> iterator = ontologyLoader.getAllclasses().iterator();
			private OWLClass currentClass = null;
			private Iterator<String> databaseIdIterator = null;

			@Override
			public boolean hasNext()
			{
				while ((currentClass == null || !databaseIdIterator.hasNext()) && iterator.hasNext())
				{
					currentClass = iterator.next();
					databaseIdIterator = ontologyLoader.getDatabaseIds(currentClass).iterator();
				}
				return databaseIdIterator.hasNext();
			}

			@Override
			public Entity next()
			{
				String ontologyTermIRI = currentClass.getIRI().toString();
				String label = databaseIdIterator.next();
				String fragments[] = label.split(":");
				MapEntity entity = new MapEntity();
				if (!referenceIds.containsKey(ontologyTermIRI))
				{
					referenceIds.put(ontologyTermIRI, new HashMap<String, String>());
				}

				if (!referenceIds.get(ontologyTermIRI).containsKey(label))
				{
					referenceIds.get(ontologyTermIRI).put(label, uuidGenerator.generateId());
				}

				entity.set(OntologyTermDynamicAnnotationMetaData.ID, referenceIds.get(ontologyTermIRI).get(label));
				entity.set(OntologyTermDynamicAnnotationMetaData.NAME, fragments[0]);
				entity.set(OntologyTermDynamicAnnotationMetaData.VALUE, fragments[1]);
				entity.set(OntologyTermDynamicAnnotationMetaData.LABEL, label);

				return entity;
			}
		};
	}

	@Override
	public void close() throws IOException
	{
		// Do nothing
	}

	@Override
	public String getName()
	{
		return OntologyTermDynamicAnnotationMetaData.ENTITY_NAME;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return OntologyTermDynamicAnnotationMetaData.getEntityMetaData();
	}

	public Map<String, Map<String, String>> getReferenceIds()
	{
		return referenceIds;
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
