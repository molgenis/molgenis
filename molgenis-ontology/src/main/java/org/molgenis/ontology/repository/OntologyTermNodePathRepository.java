package org.molgenis.ontology.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.ontology.model.OntologyTermNodePathMetaData;
import org.molgenis.ontology.utils.OWLClassContainer;
import org.molgenis.ontology.utils.OntologyLoader;
import org.semanticweb.owlapi.model.OWLClass;

import com.google.common.collect.TreeTraverser;

public class OntologyTermNodePathRepository extends AbstractRepository
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
			@Override
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
	public String getName()
	{
		return OntologyTermNodePathMetaData.ENTITY_NAME;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return OntologyTermNodePathMetaData.INSTANCE;
	}

	public Map<String, Map<String, String>> getReferenceIds()
	{
		return referenceIds;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Collections.emptySet();
	}
}
