package org.molgenis.file;

import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.RepositoryDecoratorRegistry;
import org.molgenis.file.model.FileMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static org.molgenis.file.model.FileMetaMetaData.FILE_META;

@Configuration
public class FileMetaConfig
{
	@Autowired
	RepositoryDecoratorRegistry repositoryDecoratorRegistry;

	@Autowired
	FileStore fileStore;

	@PostConstruct
	public void init()
	{
		repositoryDecoratorRegistry.addFactory(FILE_META, new RepositoryDecoratorFactory()
		{
			@Override
			public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
			{
				return (Repository<Entity>) (Repository<? extends Entity>) new FileMetaRepositoryDecorator(
						(Repository<FileMeta>) (Repository<? extends Entity>) repository, fileStore);
			}
		});
	}
}
