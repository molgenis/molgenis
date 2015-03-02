package org.molgenis.ontology;

import javax.annotation.PostConstruct;

import org.molgenis.data.FileRepositoryCollectionFactory;
import org.molgenis.data.support.GenericImporterExtensions;
import org.molgenis.ontology.repository.OntologyRepositoryCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

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
				GenericImporterExtensions.getOntology());
	}

}
