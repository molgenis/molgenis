package org.molgenis.integrationtest.data;

import org.molgenis.data.*;
import org.molgenis.data.index.IndexedRepositoryDecoratorFactory;
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
