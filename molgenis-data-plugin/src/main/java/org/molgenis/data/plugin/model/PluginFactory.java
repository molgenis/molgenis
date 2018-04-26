package org.molgenis.data.plugin.model;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.stereotype.Component;

@Component
public class PluginFactory extends AbstractSystemEntityFactory<Plugin, PluginMetadata, String>
{
	PluginFactory(PluginMetadata pluginMetadata, EntityPopulator entityPopulator)
	{
		super(Plugin.class, pluginMetadata, entityPopulator);
	}
}