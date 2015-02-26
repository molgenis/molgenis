package org.molgenis.ontology;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.ontology.utils.ZipFileUtil;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.collect.ImmutableSet;

public class OntologyRepositoryCollection extends FileRepositoryCollection
{
	private static final String EXTENSION_OBO_ZIP = "obo.zip";
	private static final String EXTENSION_OWL_ZIP = "owl.zip";
	public static final Set<String> EXTENSIONS = ImmutableSet.of(EXTENSION_OBO_ZIP, EXTENSION_OWL_ZIP);
	private final String entityName;
	private final OntologyRepository ontologyRepository;

	public OntologyRepositoryCollection(File file) throws IOException
	{
		super(EXTENSIONS);
		if (file == null) throw new IllegalArgumentException("file is null");

		String name = file.getName();
		if (name.endsWith(EXTENSION_OBO_ZIP))
		{
			this.entityName = name.substring(0, name.lastIndexOf('.' + EXTENSION_OBO_ZIP)).replace('.', '_');
		}
		else if (name.endsWith(EXTENSION_OWL_ZIP))
		{
			this.entityName = name.substring(0, name.lastIndexOf('.' + EXTENSION_OWL_ZIP)).replace('.', '_');
		}
		else
		{
			throw new IllegalArgumentException("Not a obo.zip or owl.zip file [" + file.getName() + "]");
		}

		try
		{
			List<File> uploadedFiles;
			uploadedFiles = ZipFileUtil.unzip(file);
			this.ontologyRepository = new OntologyRepository(uploadedFiles.get(0), this.entityName);
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

	@Override
	public Repository getRepositoryByEntityName(String name)
	{
		if (!entityName.equals(name)) throw new MolgenisDataException("Unknown entity name [" + name + "]");
		return this.ontologyRepository;
	}

	/**
	 * @return the ontologyRepository
	 */
	public OntologyRepository getOntologyRepository()
	{
		return ontologyRepository;
	}
}
