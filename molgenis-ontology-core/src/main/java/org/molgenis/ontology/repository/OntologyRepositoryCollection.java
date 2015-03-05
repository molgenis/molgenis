package org.molgenis.ontology.repository;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.support.GenericImporterExtensions;
import org.molgenis.ontology.utils.ZipFileUtil;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

public class OntologyRepositoryCollection extends FileRepositoryCollection
{
	private final String entityName;
	private final OntologyIndexRepository ontologyRepository;

	public OntologyRepositoryCollection(File file) throws IOException
	{
		super(GenericImporterExtensions.getOntology());
		if (file == null) throw new IllegalArgumentException("file is null");

		String name = file.getName();
		if (name.endsWith(GenericImporterExtensions.OBO_ZIP.toString()))
		{
			this.entityName = name.substring(0, name.lastIndexOf('.' + GenericImporterExtensions.OBO_ZIP.toString()))
					.replace('.', '_');
		}
		else if (name.endsWith(GenericImporterExtensions.OWL_ZIP.toString()))
		{
			this.entityName = name.substring(0, name.lastIndexOf('.' + GenericImporterExtensions.OWL_ZIP.toString()))
					.replace('.', '_');
		}
		else
		{
			throw new IllegalArgumentException("Not a obo.zip or owl.zip file [" + file.getName() + "]");
		}

		try
		{
			List<File> uploadedFiles = ZipFileUtil.unzip(file);
			this.ontologyRepository = new OntologyIndexRepository(uploadedFiles.get(0), this.entityName);
		}
		catch (OWLOntologyCreationException e)
		{
			throw new IllegalArgumentException("Not a OWLOntology file [" + file.getName() + "]");
		}
		catch (IOException e)
		{
			throw new IllegalArgumentException("Problems reading the file [" + file.getName() + "]");
		}
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return Collections.singleton(entityName);
	}

	/**
	 * @return the ontologyRepository
	 */
	public OntologyIndexRepository getOntologyRepository()
	{
		return ontologyRepository;
	}

	@Override
	public String getName()
	{
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public Repository addEntityMeta(EntityMetaData entityMeta)
	{
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public Iterator<Repository> iterator()
	{
		throw new NotImplementedException("Not implemented yet");
	}

	@Override
	public Repository getRepository(String name)
	{
		if (!entityName.equals(name)) throw new MolgenisDataException("Unknown entity name [" + name + "]");
		return this.ontologyRepository;
	}

	@Override
	public boolean hasRepository(String name)
	{
		if (null == name) return false;
		Iterator<String> entityNames = getEntityNames().iterator();
		while (entityNames.hasNext())
		{
			if (entityNames.next().equals(name)) return true;
		}
		return false;
	}
}
