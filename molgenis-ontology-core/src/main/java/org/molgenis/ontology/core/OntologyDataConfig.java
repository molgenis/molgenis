package org.molgenis.ontology.core;

import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.ontology.core.importer.repository.OntologyFileExtensions;
import org.molgenis.ontology.core.importer.repository.OntologyRepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class OntologyDataConfig
{
	@Autowired
	private FileRepositoryCollectionFactory fileRepositorySourceFactory;

	/**
	 * Registers the OntologyRepositorySource factory so it can be used by DataService.createFileRepositorySource(File
	 * file);
	 */
	@PostConstruct
	public void registerOntologyRepositorySource()
	{
		fileRepositorySourceFactory.addFileRepositoryCollectionClass(OntologyRepositoryCollection.class,
				OntologyFileExtensions.getOntology());
	}
}
