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
import org.molgenis.ontology.model.OntologyTermSynonymMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.semanticweb.owlapi.model.OWLClass;

public class OntologyTermSynonymRepository implements Repository
{
	private final OntologyLoader ontologyLoader;
	private final UuidGenerator uuidGenerator;
	private final Map<String, Map<String, String>> referenceIds = new HashMap<String, Map<String, String>>();

	public OntologyTermSynonymRepository(OntologyLoader ontologyLoader, UuidGenerator uuidGenerator)
	{
		this.ontologyLoader = ontologyLoader;
		this.uuidGenerator = uuidGenerator;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new Iterator<Entity>()
		{
			final Iterator<OWLClass> ontologyTermIterator = ontologyLoader.getAllclasses().iterator();
			private OWLClass currentClass = null;
			private Iterator<String> synonymIterator = null;

			@Override
			public boolean hasNext()
			{
				// OT_1 -> S1, S2
				// OT_2 -> S3, S4
				// OT_3 -> []
				// OT_4 -> []
				// OT_5 -> S5, S6
				while ((currentClass == null || !synonymIterator.hasNext()) && ontologyTermIterator.hasNext())
				{
					currentClass = ontologyTermIterator.next();
					synonymIterator = ontologyLoader.getSynonyms(currentClass).iterator();
				}
				return synonymIterator.hasNext() || ontologyTermIterator.hasNext();
			}

			@Override
			public Entity next()
			{
				String ontologyTermIRI = currentClass.getIRI().toString();
				String synonym = synonymIterator.next();

				MapEntity entity = new MapEntity();

				if (!referenceIds.containsKey(ontologyTermIRI))
				{
					referenceIds.put(ontologyTermIRI, new HashMap<String, String>());
				}

				if (!referenceIds.get(ontologyTermIRI).containsKey(synonym))
				{
					referenceIds.get(ontologyTermIRI).put(synonym, uuidGenerator.generateId());
				}

				entity.set(OntologyTermSynonymMetaData.ID, referenceIds.get(ontologyTermIRI).get(synonym));
				entity.set(OntologyTermSynonymMetaData.ONTOLOGY_TERM_SYNONYM, synonym);
				return entity;
			}
		};
	}

	@Override
	public void close() throws IOException
	{
		// Nothing
	}

	@Override
	public String getName()
	{
		return OntologyTermSynonymMetaData.ENTITY_NAME;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return OntologyTermSynonymMetaData.getEntityMetaData();
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
