package org.molgenis.apps.model;

import org.molgenis.app.manager.meta.App;
import org.molgenis.app.manager.meta.AppMetadata;
import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.molgenis.data.file.FileStore;

import static java.util.Objects.requireNonNull;

public class AppRepositoryDecoratorFactory extends AbstractSystemRepositoryDecoratorFactory<App, AppMetadata>
{
	private final FileStore fileStore;

	public AppRepositoryDecoratorFactory(AppMetadata appMetadata, FileStore fileStore)
	{
		super(appMetadata);
		this.fileStore = requireNonNull(fileStore);
	}

	@Override
	public Repository<App> createDecoratedRepository(Repository<App> repository)
	{
		return new AppRepositoryDecorator(repository, fileStore);
	}
}
