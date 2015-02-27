package org.molgenis.ontology.repository.v2;

import java.io.IOException;
import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.ontology.model.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.semanticweb.owlapi.model.OWLClass;

public class OntologyTermDynamicAnnotationRepository implements Repository
{
	private final OntologyLoader ontologyLoader;

	public OntologyTermDynamicAnnotationRepository(OntologyLoader ontologyLoader)
	{
		this.ontologyLoader = ontologyLoader;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		final Iterator<OWLClass> iterator = ontologyLoader.getAllclasses().iterator();

		return new Iterator<Entity>()
		{
			private OWLClass currentClass = null;
			private Iterator<String> databaseIdIterator = null;

			@Override
			public boolean hasNext()
			{
				if ((currentClass == null || !databaseIdIterator.hasNext()) && iterator.hasNext())
				{
					currentClass = iterator.next();
					databaseIdIterator = ontologyLoader.getSynonyms(currentClass).iterator();
				}
				return databaseIdIterator.hasNext();
			}

			@Override
			public Entity next()
			{
				String ontologyIRI = ontologyLoader.getOntologyIRI();
				String ontologyTermIRI = currentClass.getIRI().toString();
				String label = databaseIdIterator.next();
				String fragments[] = label.split(":");
				String id = OntologyRepositoryCollection.createUniqueId(ontologyIRI, ontologyTermIRI, label);

				MapEntity entity = new MapEntity();
				entity.set(OntologyTermDynamicAnnotationMetaData.ID, id);
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
}
