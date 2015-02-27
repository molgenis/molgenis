package org.molgenis.ontology.repository.v2;

import java.io.IOException;
import java.util.Iterator;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.Repository;
import org.molgenis.data.support.MapEntity;
import org.molgenis.ontology.model.OntologyTermSynonymMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.semanticweb.owlapi.model.OWLClass;

public class OntologyTermSynonymRepository implements Repository
{
	private final OntologyLoader ontologyLoader;

	public OntologyTermSynonymRepository(OntologyLoader ontologyLoader)
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
			private Iterator<String> synonymIterator = null;

			@Override
			public boolean hasNext()
			{
				if ((currentClass == null || !synonymIterator.hasNext()) && iterator.hasNext())
				{
					currentClass = iterator.next();
					synonymIterator = ontologyLoader.getSynonyms(currentClass).iterator();
				}
				return synonymIterator.hasNext();
			}

			@Override
			public Entity next()
			{
				String ontologyIRI = ontologyLoader.getOntologyIRI();
				String ontologyTermIRI = currentClass.getIRI().toString();
				String synonym = synonymIterator.next();
				String id = OntologyRepositoryCollection.createUniqueId(ontologyIRI, ontologyTermIRI, synonym);

				MapEntity entity = new MapEntity();
				entity.set(OntologyTermSynonymMetaData.ID, id);
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
