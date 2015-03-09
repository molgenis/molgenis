package org.molgenis.ontology.repository;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.poi.ss.formula.eval.NotImplementedException;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Repository;
import org.molgenis.data.support.FileRepositoryCollection;
import org.molgenis.data.support.GenericImporterExtensions;
import org.molgenis.data.support.UuidGenerator;
import org.molgenis.ontology.model.OntologyMetaData;
import org.molgenis.ontology.model.OntologyTermDynamicAnnotationMetaData;
import org.molgenis.ontology.model.OntologyTermMetaData;
import org.molgenis.ontology.model.OntologyTermNodePathMetaData;
import org.molgenis.ontology.model.OntologyTermSynonymMetaData;
import org.molgenis.ontology.utils.OntologyLoader;
import org.molgenis.ontology.utils.ZipFileUtil;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.springframework.beans.factory.annotation.Autowired;

public class OntologyRepositoryCollection extends FileRepositoryCollection
{
	@Autowired
	private DataService dataService;

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

		List<File> uploadedFiles = ZipFileUtil.unzip(file);
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
		repositories.put(OntologyMetaData.ENTITY_NAME, new OntologyRepository(ontologyLoader, uuidGenerator));
		repositories.put(OntologyTermMetaData.ENTITY_NAME, new OntologyTermRepository(ontologyLoader, uuidGenerator,
				ontologyTermDynamicAnnotationRepo, ontologyTermSynonymRepo, ontologyTermNodePathRepository, null));
	}

	@Override
	public Iterable<String> getEntityNames()
	{
		return repositories.keySet();
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
		if (!repositories.containsKey(name)) throw new MolgenisDataException("Unknown entity name [" + name + "]");
		return repositories.get(name);
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
