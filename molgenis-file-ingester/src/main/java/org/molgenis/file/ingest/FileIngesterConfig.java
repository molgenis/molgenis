package org.molgenis.file.ingest;

import static org.molgenis.file.ingest.meta.FileIngestMetaData.FILE_INGEST;

import javax.annotation.PostConstruct;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.support.TypedRepositoryDecorator;
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
		repositoryDecoratorRegistry.addFactory(FILE_INGEST, new RepositoryDecoratorFactory()
		{
			@Override
			public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
			{
				return new FileIngestRepositoryDecorator(repository, fileIngesterJobScheduler, dataService);
			}

			@Override
			public <E extends Entity> Repository<E> createDecoratedRepository(Repository<E> repository,
					Class<E> entityClass)
			{
				Repository<Entity> decoratedRepository = createDecoratedRepository((Repository<Entity>) repository);
				return new TypedRepositoryDecorator<>(decoratedRepository, entityClass);
			}
		});
	}
}
