package org.molgenis.web.bootstrap;

import com.google.common.collect.ImmutableMap;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginFactory;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.PluginController;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;
import static org.molgenis.data.plugin.model.PluginMetadata.PLUGIN;

public class PluginPopulatorTest extends AbstractMockitoTest
{
	@Mock
	private DataService dataService;

	@Mock
	private PluginFactory pluginFactory;

	@Mock
	private Repository<Plugin> pluginRepository;

	private PluginPopulator pluginPopulator;

	@Test
	public void testPopulateWithNewPlugin() throws Exception
	{
		PluginController pluginController1 = mock(PluginController.class);
		when(pluginController1.getId()).thenReturn("plugin1");

		PluginController pluginController2 = mock(PluginController.class);
		when(pluginController2.getId()).thenReturn("plugin2");

		PluginController pluginController3 = mock(PluginController.class);
		when(pluginController3.getId()).thenReturn("plugin3");

		ApplicationContext ctx = mock(ApplicationContext.class);
		when(ctx.getBeansOfType(PluginController.class)).thenReturn(
				ImmutableMap.of("plugin1", pluginController1, "plugin2", pluginController2, "plugin3",
						pluginController3));

		Plugin existingPlugin1 = mock(Plugin.class);
		when(existingPlugin1.getId()).thenReturn("plugin1");
		when(existingPlugin1.setLabel("plugin1")).thenReturn(existingPlugin1);

		Plugin existingPlugin2 = mock(Plugin.class);
		when(existingPlugin2.getId()).thenReturn("plugin2");
		when(existingPlugin2.setLabel("plugin2")).thenReturn(existingPlugin2);

		Plugin newPlugin = mock(Plugin.class);
		when(newPlugin.getId()).thenReturn("plugin3");
		when(newPlugin.setLabel("plugin3")).thenReturn(newPlugin);

		when(pluginFactory.create("plugin1")).thenReturn(existingPlugin1);
		when(pluginFactory.create("plugin2")).thenReturn(existingPlugin2);
		when(pluginFactory.create("plugin3")).thenReturn(newPlugin);

		when(dataService.getRepository(PLUGIN, Plugin.class)).thenReturn(pluginRepository);
		when(dataService.findAll(PLUGIN, Plugin.class)).thenReturn(Stream.of(existingPlugin1, existingPlugin2));

		pluginPopulator = new PluginPopulator(dataService, pluginFactory);
		pluginPopulator.populate(ctx);

		// Verify that the new plugin present in the context is written to the database
		verify(pluginRepository).upsertBatch(newArrayList(newPlugin, existingPlugin1, existingPlugin2));
	}

	@Test
	public void testPopulateWithContextDeletedPlugin() throws Exception
	{
		ApplicationContext ctx = mock(ApplicationContext.class);

		PluginController pluginController1 = mock(PluginController.class);
		PluginController pluginController2 = mock(PluginController.class);

		Plugin existingPlugin1 = mock(Plugin.class);
		Plugin existingPlugin2 = mock(Plugin.class);
		Plugin existingPlugin3 = mock(Plugin.class);

		when(pluginController1.getId()).thenReturn("plugin1");
		when(pluginController2.getId()).thenReturn("plugin2");

		when(ctx.getBeansOfType(PluginController.class)).thenReturn(
				ImmutableMap.of("plugin1", pluginController1, "plugin2", pluginController2));

		when(existingPlugin1.getId()).thenReturn("plugin1");
		when(existingPlugin2.getId()).thenReturn("plugin2");
		when(existingPlugin3.getId()).thenReturn("plugin3");

		when(existingPlugin1.setLabel("plugin1")).thenReturn(existingPlugin1);
		when(existingPlugin2.setLabel("plugin2")).thenReturn(existingPlugin2);

		when(pluginFactory.create("plugin1")).thenReturn(existingPlugin1);
		when(pluginFactory.create("plugin2")).thenReturn(existingPlugin2);

		when(dataService.getRepository(PLUGIN, Plugin.class)).thenReturn(pluginRepository);
		when(dataService.findAll(PLUGIN, Plugin.class)).thenReturn(
				Stream.of(existingPlugin1, existingPlugin2, existingPlugin3));

		pluginPopulator = new PluginPopulator(dataService, pluginFactory);
		pluginPopulator.populate(ctx);

		// Verify that plugin3 is deleted from the DB because it was no longer present in the context
		verify(dataService).delete(PLUGIN, existingPlugin3);

		// Verify that the new plugin present in the context is written to the database
		verify(pluginRepository).upsertBatch(newArrayList(existingPlugin1, existingPlugin2));
	}
}