package org.molgenis.ontology.repository;

import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.ontology.model.OntologyMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class OntologyRepository extends AbstractRepository
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
		return OntologyMetaData.INSTANCE;
	}

	@Override
	public Set<RepositoryCapability> getCapabilities()
	{
		return Collections.emptySet();
	}

}