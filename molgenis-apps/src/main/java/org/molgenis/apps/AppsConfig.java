package org.molgenis.apps;

import org.molgenis.apps.model.App;
import org.molgenis.apps.model.AppRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorFactory;
import org.molgenis.data.platform.decorators.RepositoryDecoratorRegistry;
import org.molgenis.file.FileStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static org.molgenis.apps.model.AppMetaData.APP;

@Configuration
public class AppsConfig
{
	@Autowired
	FileStore fileStore;

	@Autowired
	RepositoryDecoratorRegistry repositoryDecoratorRegistry;

	@PostConstruct
	public void init()
	{
		repositoryDecoratorRegistry.addFactory(APP, new RepositoryDecoratorFactory()
		{
			@Override
			public Repository<Entity> createDecoratedRepository(Repository<Entity> repository)
			{
				return (Repository<Entity>) (Repository<? extends Entity>) new AppRepositoryDecorator(
						(Repository<App>) (Repository<? extends Entity>) repository, fileStore);
			}
		});
	}
}
