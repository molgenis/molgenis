package org.molgenis.ontology.repository.v2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
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
				String id = uuidGenerator.generateId();

				if (!referenceIds.containsKey(ontologyTermIRI))
				{
					referenceIds.put(ontologyTermIRI, new HashMap<String, String>());
				}

				if (!referenceIds.get(ontologyTermIRI).containsKey(label))
				{
					referenceIds.get(ontologyTermIRI).put(label, id);
				}

				MapEntity entity = new MapEntity();
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

	public Map<String, Map<String, String>> getReferenceIds()
	{
		return referenceIds;
	}
}
