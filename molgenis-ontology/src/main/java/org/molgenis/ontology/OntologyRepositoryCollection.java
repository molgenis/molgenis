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
	static final Set<String> EXTENSIONS = ImmutableSet.of(EXTENSION_OBO_ZIP);

	private final File file;
	private final String entityName;

	public OntologyRepositoryCollection(File file) throws IOException
	{
		super(EXTENSIONS);
		if (file == null) throw new IllegalArgumentException("file is null");
		this.file = file;

		String name = file.getName();
		if (name.endsWith(EXTENSION_OBO_ZIP))
		{
			this.entityName = name.substring(0, name.lastIndexOf('.' + EXTENSION_OBO_ZIP));
		}
		else
		{
			throw new IllegalArgumentException("Not a OBO.ZIP file [" + file.getName() + "]");
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
		try
		{
			List<File> uploadedFiles;
			uploadedFiles = ZipFileUtil.unzip(file);
			return new OntologyRepository(uploadedFiles.get(0), name);
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
}
