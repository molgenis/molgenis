package org.molgenis.file.ingest;

import javax.annotation.PostConstruct;

import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.file.ingest.meta.FileIngestMetaData;
import org.molgenis.ui.RepositoryDecoratorRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

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
		repositoryDecoratorRegistry.addFactory(FileIngestMetaData.ENTITY_NAME, new RepositoryDecoratorFactory()
		{
			@Override
			public Repository createDecoratedRepository(Repository repository)
			{
				return new FileIngestRepositoryDecorator(repository, fileIngesterJobScheduler, dataService);
			}
		});
	}
}
