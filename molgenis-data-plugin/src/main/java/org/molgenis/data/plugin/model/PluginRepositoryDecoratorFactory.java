package org.molgenis.data.plugin.model;

import org.molgenis.data.AbstractSystemRepositoryDecoratorFactory;
import org.molgenis.data.Repository;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class PluginRepositoryDecoratorFactory extends AbstractSystemRepositoryDecoratorFactory<Plugin, PluginMetadata>
{
	private final MutableAclService mutableAclService;

	public PluginRepositoryDecoratorFactory(PluginMetadata pluginMetadata, MutableAclService mutableAclService)
	{
		super(pluginMetadata);
		this.mutableAclService = requireNonNull(mutableAclService);
	}

	@Override
	public Repository<Plugin> createDecoratedRepository(Repository<Plugin> repository)
	{
		return new PluginSecurityRepositoryDecorator(repository, mutableAclService);
	}
}
