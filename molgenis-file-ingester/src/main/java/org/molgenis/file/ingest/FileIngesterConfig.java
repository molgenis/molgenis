package org.molgenis.file.ingest;

import org.molgenis.data.*;
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
		repositoryDecoratorRegistry.addFactory(FILE_INGEST, new RepositoryDecoratorFactory()
		{
			@Override
			public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
			{
				return new FileIngestRepositoryDecorator(repository, fileIngesterJobScheduler, dataService);
			}
		});
	}
}
