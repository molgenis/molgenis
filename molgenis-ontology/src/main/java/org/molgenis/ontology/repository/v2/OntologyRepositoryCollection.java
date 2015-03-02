package org.molgenis.ontology.repository.v2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.ontology.model.OntologyMetaData;
import org.molgenis.ontology.model.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.model.OntologyTermMetaData;
import org.molgenis.ontology.model.OntologyTermNodePathMetaData;
import org.molgenis.ontology.model.OntologyTermSynonymMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.molgenis.ontology.utils.ZipFileUtil;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.collect.ImmutableSet;

public class OntologyRepositoryCollection extends FileRepositoryCollection
{
	private static final String EXTENSION_OBO_ZIP = "obo.zip";
	private static final String EXTENSION_OWL_ZIP = "owl.zip";
	private static final String ESCAPE_VALUES = "[^a-zA-Z0-9_]";
	private static final String REPLACEMENT_VALUE = "_";
	public static final Set<String> EXTENSIONS = ImmutableSet.of(EXTENSION_OBO_ZIP, EXTENSION_OWL_ZIP);

	private LinkedHashMap<String, Repository> repositories;

	public OntologyRepositoryCollection(File file) throws OWLOntologyCreationException, FileNotFoundException,
			IOException
	{
		super(EXTENSIONS);
		if (file == null) throw new IllegalArgumentException("file is null");

		String name = file.getName();
		if (name.endsWith(EXTENSION_OBO_ZIP))
		{
			name = name.substring(0, name.lastIndexOf('.' + EXTENSION_OBO_ZIP)).replace('.', '_');
		}
		else if (name.endsWith(EXTENSION_OWL_ZIP))
		{
			name = name.substring(0, name.lastIndexOf('.' + EXTENSION_OWL_ZIP)).replace('.', '_');
		}
		else
		{
			throw new IllegalArgumentException("Not a obo.zip or owl.zip file [" + file.getName() + "]");
		}

		List<File> uploadedFiles;
		uploadedFiles = ZipFileUtil.unzip(file);
		OntologyLoader ontologyLoader = new OntologyLoader(name, uploadedFiles.get(0));

		UuidGenerator uuidGenerator = new UuidGenerator();
		OntologyTermDynamicAnnotationRepository ontologyTermDynamicAnnotationRepo = new OntologyTermDynamicAnnotationRepository(
				ontologyLoader, uuidGenerator);
		OntologyTermSynonymRepository ontologyTermSynonymRepo = new OntologyTermSynonymRepository(ontologyLoader,
				uuidGenerator);
		OntologyTermNodePathRepository ontologyTermNodePathRepository = new OntologyTermNodePathRepository(
				ontologyLoader, uuidGenerator);

		repositories = new LinkedHashMap<String, Repository>();
		repositories.put(OntologyTermDynamicAnnotationMetaData.ENTITY_NAME, ontologyTermDynamicAnnotationRepo);
		repositories.put(OntologyTermSynonymMetaData.ENTITY_NAME, ontologyTermSynonymRepo);
		repositories.put(OntologyTermNodePathMetaData.ENTITY_NAME, ontologyTermNodePathRepository);
		repositories.put(OntologyMetaData.ENTITY_NAME, new OntologyRepository(ontologyLoader));
		repositories.put(OntologyTermMetaData.ENTITY_NAME, new OntologyTermRepository(ontologyLoader, uuidGenerator,
				ontologyTermDynamicAnnotationRepo, ontologyTermSynonymRepo, ontologyTermNodePathRepository));
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
		return escapeValue(stringBuilder.toString());
	}

	public static String escapeValue(String value)
	{
		return value.replaceAll(ESCAPE_VALUES, REPLACEMENT_VALUE);
	}
}
