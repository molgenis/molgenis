package org.molgenis.ontology.repository.v2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.support.GenericImporterExtensions;
import org.molgenis.ontology.model.OntologyMetaData;
import org.molgenis.ontology.model.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.model.OntologyTermMetaData;
import org.molgenis.ontology.model.OntologyTermSynonymMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.molgenis.ontology.utils.ZipFileUtil;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.annotation.Autowired;

public class OntologyRepositoryCollection extends FileRepositoryCollection
{
	@Autowired
	private DataService dataService;

	private static final String ESCAPE_VALUES = "[^a-zA-Z0-9_]";

	private LinkedHashMap<String, Repository> repositories;

	public OntologyRepositoryCollection(File file) throws OWLOntologyCreationException, FileNotFoundException,
			IOException
	{
		super(GenericImporterExtensions.getOntology());
		if (file == null) throw new IllegalArgumentException("file is null");

		String name = file.getName();
		if (name.endsWith(GenericImporterExtensions.OBO_ZIP.toString()))
		{
			name = name.substring(0, name.lastIndexOf('.' + GenericImporterExtensions.OBO_ZIP.toString())).replace('.',
					'_');
		}
		else if (name.endsWith(GenericImporterExtensions.OWL_ZIP.toString()))
		{
			name = name.substring(0, name.lastIndexOf('.' + GenericImporterExtensions.OWL_ZIP.toString())).replace('.',
					'_');
		}
		else
		{
			throw new IllegalArgumentException("Not a obo.zip or owl.zip file [" + file.getName() + "]");
		}

		List<File> uploadedFiles;
		uploadedFiles = ZipFileUtil.unzip(file);
		OntologyLoader ontologyLoader = new OntologyLoader(name, uploadedFiles.get(0));

		repositories = new LinkedHashMap<String, Repository>();
		repositories.put(OntologyTermDynamicAnnotationMetaData.ENTITY_NAME,
				new OntologyTermDynamicAnnotationRepository(ontologyLoader));
		repositories.put(OntologyTermSynonymMetaData.ENTITY_NAME, new OntologyTermSynonymRepository(ontologyLoader));
		repositories.put(OntologyMetaData.ENTITY_NAME, new OntologyRepository(ontologyLoader));
		repositories.put(OntologyTermMetaData.ENTITY_NAME, new OntologyTermRepository(ontologyLoader, dataService));
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return repositories.keySet();
	}

	@Override
	public Repository getRepositoryByEntityName(String name)
	{
		if (!repositories.containsKey(name)) throw new MolgenisDataException("Unknown entity name [" + name + "]");
		return repositories.get(name);
	}

	public static String createUniqueId(String ontologyIRI, String ontologyTermIRI, String label)
	{
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(ontologyIRI).append(ontologyTermIRI).append(label);
		return stringBuilder.toString().replaceAll(ESCAPE_VALUES, StringUtils.EMPTY);
	}
}
