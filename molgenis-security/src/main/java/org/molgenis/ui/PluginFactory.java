package org.molgenis.ui;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PluginFactory extends AbstractSystemEntityFactory<Plugin, PluginMetadata, String>
{
	@Autowired
	PluginFactory(PluginMetadata pluginMetadata, EntityPopulator entityPopulator)
	{
		super(Plugin.class, pluginMetadata, entityPopulator);
	}
}
