package org.molgenis.data.platform.config;

import static java.util.Objects.requireNonNull;

import javax.annotation.PostConstruct;
import org.molgenis.data.DataService;
import org.molgenis.data.EntityFactoryRegistry;
import org.molgenis.data.EntityManagerImpl;
import org.molgenis.data.config.DataConfig;
import org.molgenis.data.listeners.EntityListenersService;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.platform.decorators.SystemRepositoryDecoratorRegistryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({
  DataConfig.class,
  EntityManagerImpl.class,
  SystemRepositoryDecoratorRegistryImpl.class,
  EntityFactoryRegistry.class,
  EntityListenersService.class
})
public class PlatformConfig {
  private final DataService dataService;
  private final MetaDataService metaDataService;

  @Autowired
  public PlatformConfig(DataService dataService, MetaDataService metaDataService) {
    this.dataService = requireNonNull(dataService);
    this.metaDataService = requireNonNull(metaDataService);
  }

  @PostConstruct
  public void init() {
    dataService.setMetaDataService(metaDataService);
  }
}
