package org.molgenis.ontology.repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.AggregateQuery;
import org.molgenis.data.AggregateResult;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.ontology.model.OntologyTermNodePathMetaData;
import org.molgenis.ontology.utils.OWLClassContainer;
import org.molgenis.ontology.utils.OntologyLoader;
import org.semanticweb.owlapi.model.OWLClass;

import com.google.common.collect.TreeTraverser;

public class OntologyTermNodePathRepository implements Repository
{
	private final static String PSEUDO_ROOT_CLASS_LABEL = "top";
	private final OntologyLoader ontologyLoader;
	private final UuidGenerator uuidGenerator;
	private final Map<String, Map<String, String>> referenceIds = new HashMap<String, Map<String, String>>();

	public OntologyTermNodePathRepository(OntologyLoader ontologyLoader, UuidGenerator uuidGenerator)
	{
		this.ontologyLoader = ontologyLoader;
		this.uuidGenerator = uuidGenerator;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		final TreeTraverser<OWLClassContainer> traverser = new TreeTraverser<OWLClassContainer>()
		{
			public Iterable<OWLClassContainer> children(OWLClassContainer container)
			{
				int count = 0;
				List<OWLClassContainer> containers = new ArrayList<OWLClassContainer>();
				for (OWLClass childClass : ontologyLoader.getChildClass(container.getOwlClass()))
				{
					containers.add(new OWLClassContainer(childClass, constructNodePath(container.getNodePath(), count),
							false));
					count++;
				}
				return containers;
			}
		};
		return new Iterator<Entity>()
		{
			final OWLClass pseudoRootClass = ontologyLoader.createClass(PSEUDO_ROOT_CLASS_LABEL,
					ontologyLoader.getRootClasses());

			private final Iterator<OWLClassContainer> iterator = traverser.preOrderTraversal(
					new OWLClassContainer(pseudoRootClass, "0[0]", true)).iterator();

			@Override
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			@Override
			public Entity next()
			{
				OWLClassContainer container = iterator.next();
				String ontologyTermIRI = container.getOwlClass().getIRI().toString();
				String ontologyTermNodePath = container.getNodePath();

				MapEntity entity = new MapEntity();

				if (!referenceIds.containsKey(ontologyTermIRI))
				{
					referenceIds.put(ontologyTermIRI, new HashMap<String, String>());
				}

				if (!referenceIds.get(ontologyTermIRI).containsKey(ontologyTermNodePath))
				{
					referenceIds.get(ontologyTermIRI).put(ontologyTermNodePath, uuidGenerator.generateId());
				}

				entity.set(OntologyTermNodePathMetaData.ID, referenceIds.get(ontologyTermIRI).get(ontologyTermNodePath));
				entity.set(OntologyTermNodePathMetaData.ONTOLOGY_TERM_NODE_PATH, ontologyTermNodePath);
				entity.set(OntologyTermNodePathMetaData.ROOT, container.isRoot());

				return entity;
			}
		};
	}

	private String constructNodePath(String parentNodePath, int currentPosition)
	{
		StringBuilder nodePathStringBuilder = new StringBuilder();
		if (!StringUtils.isEmpty(parentNodePath)) nodePathStringBuilder.append(parentNodePath).append('.');
		nodePathStringBuilder.append(currentPosition).append('[')
				.append(nodePathStringBuilder.toString().split("\\.").length - 1).append(']');
		return nodePathStringBuilder.toString();
	}

	@Override
	public void close() throws IOException
	{
		// Do nothing
	}

	@Override
	public String getName()
	{
		return OntologyTermNodePathMetaData.ENTITY_NAME;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return OntologyTermNodePathMetaData.getEntityMetaData();
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
