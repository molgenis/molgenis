package org.molgenis.apps.model;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.file.FileStore;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class AppRepositoryDecoratorFactory extends AbstractSystemRepositoryDecoratorFactory<App, AppMetaData>
{
	private final FileStore fileStore;

	public AppRepositoryDecoratorFactory(AppMetaData appMetaData, FileStore fileStore)
	{
		super(appMetaData);
		this.fileStore = requireNonNull(fileStore);
	}

	@Override
	public Repository<App> createDecoratedRepository(Repository<App> repository)
	{
		return new AppRepositoryDecorator(repository, fileStore);
	}
}
