package org.molgenis.data.plugin.model;

import static java.util.Objects.requireNonNull;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.molgenis.settings.AppSettings;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Component;

@Component
public class PluginRepositoryDecoratorFactory
    extends AbstractSystemRepositoryDecoratorFactory<Plugin, PluginMetadata> {
  private final MutableAclService mutableAclService;
  private final AppSettings appSettings;

  public PluginRepositoryDecoratorFactory(
      PluginMetadata pluginMetadata, MutableAclService mutableAclService, AppSettings appSettings) {
    super(pluginMetadata);
    this.mutableAclService = requireNonNull(mutableAclService);
    this.appSettings = requireNonNull(appSettings);
  }

  @Override
  public Repository<Plugin> createDecoratedRepository(Repository<Plugin> repository) {
    return new PluginSecurityRepositoryDecorator(
        new PluginRepositoryDecorator(repository, appSettings), mutableAclService);
  }
}
