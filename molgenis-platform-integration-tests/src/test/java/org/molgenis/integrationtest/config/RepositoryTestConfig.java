package org.molgenis.integrationtest.config;

import org.molgenis.data.RepositoryCollectionBootstrapper;
import org.molgenis.data.RepositoryCollectionRegistry;
import org.molgenis.data.SystemRepositoryDecoratorFactoryRegistrar;
import org.molgenis.data.file.FileRepositoryCollectionFactory;
import org.molgenis.data.index.IndexedRepositoryDecoratorFactory;
import org.molgenis.data.platform.RepositoryCollectionDecoratorFactoryImpl;
import org.molgenis.data.platform.decorators.MolgenisRepositoryDecoratorFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ RepositoryCollectionRegistry.class, RepositoryCollectionDecoratorFactoryImpl.class,
		RepositoryCollectionBootstrapper.class, IndexedRepositoryDecoratorFactory.class,
		MolgenisRepositoryDecoratorFactory.class, FileRepositoryCollectionFactory.class,
		SystemRepositoryDecoratorFactoryRegistrar.class })
public class RepositoryTestConfig
{
}
