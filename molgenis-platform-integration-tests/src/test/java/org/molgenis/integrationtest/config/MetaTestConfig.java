package org.molgenis.integrationtest.config;

import javax.annotation.PostConstruct;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityFactoryRegistrar;
import org.molgenis.data.EntityFactoryRegistry;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.config.DataConfig;
import org.molgenis.data.i18n.SystemEntityTypeI18nInitializer;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.meta.EntityTypeDependencyResolver;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.persist.PackagePersister;
import org.molgenis.data.meta.system.SystemEntityTypeInitializer;
import org.molgenis.data.meta.system.SystemEntityTypePersister;
import org.molgenis.data.meta.system.SystemEntityTypeRegistrar;
import org.molgenis.data.meta.system.SystemPackageRegistrar;
import org.molgenis.data.meta.system.SystemPackageRegistry;
import org.molgenis.data.platform.decorators.SystemRepositoryDecoratorRegistryImpl;
import org.molgenis.data.security.SystemEntityTypeRegistryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Uses {@link ComponentScan} because of unclarified issues in metadata model. */
@Configuration
@Import({
  DataConfig.class,
  EntityManagerImpl.class,
  SystemRepositoryDecoratorRegistryImpl.class,
  EntityFactoryRegistry.class,
  EntityListenersService.class,
  AttributeFactory.class,
  SystemEntityTypeRegistryImpl.class,
  EntityTypeDependencyResolver.class,
  SystemPackageRegistry.class,
  PackagePersister.class,
  SystemEntityTypePersister.class,
  SystemEntityTypeRegistrar.class,
  SystemPackageRegistrar.class,
  EntityFactoryRegistrar.class,
  SystemEntityTypeInitializer.class,
  SystemEntityTypeI18nInitializer.class
})
@ComponentScan("org.molgenis.data.meta.model")
public class MetaTestConfig {
  @Autowired private MetaDataService metaDataService;

  @Autowired private DataService dataService;

  @PostConstruct
  public void init() {
    dataService.setMetaDataService(metaDataService);
  }
}
