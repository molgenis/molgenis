package org.molgenis.apps;

import org.molgenis.apps.model.App;
import org.molgenis.apps.model.AppRepositoryDecorator;
import org.molgenis.data.Entity;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryDecoratorRegistry;
import org.molgenis.file.FileStore;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import static java.util.Objects.requireNonNull;
import static org.molgenis.apps.model.AppMetaData.APP;

@Configuration
public class AppsConfig
{
	private final FileStore fileStore;
	private final RepositoryDecoratorRegistry repositoryDecoratorRegistry;

	public AppsConfig(FileStore fileStore, RepositoryDecoratorRegistry repositoryDecoratorRegistry)
	{

		this.fileStore = requireNonNull(fileStore);
		this.repositoryDecoratorRegistry = requireNonNull(repositoryDecoratorRegistry);
	}

	@SuppressWarnings("unchecked")
	@PostConstruct
	public void init()
	{
		repositoryDecoratorRegistry.addFactory(APP,
				repository -> (Repository<Entity>) (Repository<? extends Entity>) new AppRepositoryDecorator(
						(Repository<App>) (Repository<? extends Entity>) repository, fileStore));
	}
}
