package org.molgenis.ontology;

import java.io.File;
import java.util.Iterator;

import org.molgenis.data.Countable;
import org.molgenis.data.Entity;
import org.molgenis.data.elasticsearch.util.MapperTypeSanitizer;
import org.molgenis.data.support.MapEntity;
import org.molgenis.ontology.repository.AbstractOntologyRepository;
import org.molgenis.ontology.utils.OntologyLoader;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class OntologyRepository extends AbstractOntologyRepository implements Countable
{
	private OntologyLoader ontologyLoader;
	private File file;

	public OntologyRepository(File file, String name) throws OWLOntologyCreationException
	{
		super(name);
		this.setFile(file);
		this.ontologyLoader = new OntologyLoader(name, file);
	}

	@Override
	public Iterator<Entity> iterator()
	{
		if (ontologyLoader == null) throw new IllegalArgumentException("OntologyLoader is null!");

		return new Iterator<Entity>()
		{
			private int count = 0;

			@Override
			public boolean hasNext()
			{
				if (count < count())
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
				entity.set(ID, MapperTypeSanitizer.sanitizeMapperType(ontologyLoader.getOntologyName()));
				entity.set(ONTOLOGY_IRI, ontologyLoader.getOntologyIRI());
				entity.set(ONTOLOGY_NAME, ontologyLoader.getOntologyName());
				entity.set(ENTITY_TYPE, TYPE_ONTOLOGY);
				return entity;
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}

		};
	}

	public long count()
	{
		return 1;
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

	/**
	 * @return the file
	 */
	public File getFile()
	{
		return file;
	}

	/**
	 * @param file
	 *            the file to set
	 */
	public void setFile(File file)
	{
		this.file = file;
	}

	/**
	 * @return the ontologyLoader
	 */
	public OntologyLoader getOntologyLoader()
	{
		return ontologyLoader;
	}

	/**
	 * @param ontologyLoader
	 *            the ontologyLoader to set
	 */
	public void setOntologyLoader(OntologyLoader ontologyLoader)
	{
		this.ontologyLoader = ontologyLoader;
	}
}
