package org.molgenis.file.ingest;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.platform.decorators.RepositoryDecoratorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static org.molgenis.file.ingest.meta.FileIngestMetaData.FILE_INGEST;

@Configuration
public class FileIngesterConfig
{
	@Autowired
	RepositoryDecoratorRegistry repositoryDecoratorRegistry;

	@Autowired
	FileIngesterJobScheduler fileIngesterJobScheduler;

	@Autowired
	DataService dataService;

	@PostConstruct
	public void init()
	{
		// Decorate FileIngest repository
		repositoryDecoratorRegistry.addFactory(FILE_INGEST,
				repository -> new FileIngestRepositoryDecorator(repository, fileIngesterJobScheduler, dataService));
	}
}
