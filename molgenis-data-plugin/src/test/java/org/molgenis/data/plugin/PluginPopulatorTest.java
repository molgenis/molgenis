package org.molgenis.data.plugin;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginFactory;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class PluginPopulatorTest extends AbstractMockitoTest
{
	@Mock
	private DataService dataService;
	@Mock
	private PluginRegistry pluginRegistry;
	@Mock
	private PluginFactory pluginFactory;
	@Captor
	private ArgumentCaptor<List<Plugin>> updateStreamArgumentCaptor;
	@Captor
	private ArgumentCaptor<Stream<Plugin>> deleteStreamArgumentCaptor;
	@Mock
	private Repository<Plugin> pluginRepository;

	private PluginPopulator pluginPopulator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		when(dataService.getRepository(PluginMetadata.PLUGIN, Plugin.class)).thenReturn(pluginRepository);
		pluginPopulator = new PluginPopulator(dataService, pluginRegistry, pluginFactory);
	}

	@Test
	public void testPopulate() throws Exception
	{
		Plugin newPluginEntity = mock(Plugin.class);
		org.molgenis.data.plugin.Plugin newPlugin = new org.molgenis.data.plugin.Plugin("plugin0", "Plugin #0", "uri",
				"fullUri");
		when(pluginRegistry.getPlugin("plugin0")).thenReturn(newPlugin);
		when(pluginFactory.create("plugin0")).thenReturn(newPluginEntity);

		Plugin changedPluginEntity = mock(Plugin.class);
		org.molgenis.data.plugin.Plugin changedPlugin = new org.molgenis.data.plugin.Plugin("plugin1",
				"Plugin #1 - updated", "uri", "fullUri");
		when(pluginRegistry.getPlugin("plugin1")).thenReturn(changedPlugin);
		when(pluginFactory.create("plugin1")).thenReturn(changedPluginEntity);
		when(pluginRegistry.getPlugins()).thenReturn(asList(newPlugin, changedPlugin));

		Plugin existingChangedPlugin = mock(Plugin.class);
		when(existingChangedPlugin.getId()).thenReturn("plugin1");
		when(existingChangedPlugin.getLabel()).thenReturn("Plugin #1");
		when(existingChangedPlugin.getUri()).thenReturn("uri");
		when(existingChangedPlugin.getFullUri()).thenReturn("fullUri");

		Plugin deletedPlugin = mock(Plugin.class);
		when(deletedPlugin.getId()).thenReturn("plugin2");
		when(deletedPlugin.getLabel()).thenReturn("Plugin #2");
		when(deletedPlugin.getUri()).thenReturn("uri");
		when(deletedPlugin.getFullUri()).thenReturn("fullUri");

		when(dataService.getRepository(PluginMetadata.PLUGIN, Plugin.class)).thenReturn(pluginRepository);
		when(dataService.findAll(PluginMetadata.PLUGIN, Plugin.class)).thenReturn(
				Stream.of(existingChangedPlugin, deletedPlugin));

		pluginPopulator.populate();

		verify(pluginRepository).upsertBatch(updateStreamArgumentCaptor.capture());
		assertEquals(updateStreamArgumentCaptor.getValue(), asList(newPluginEntity, changedPluginEntity));
		verify(dataService).delete(eq(PluginMetadata.PLUGIN), deleteStreamArgumentCaptor.capture());
		assertEquals(deleteStreamArgumentCaptor.getValue().collect(toList()), singletonList(deletedPlugin));
	}

	@Test
	public void testPopulateDoNothing() throws Exception
	{
		org.molgenis.data.plugin.Plugin molgenisPlugin0 = new org.molgenis.data.plugin.Plugin("plugin0", "Plugin #0",
				"uri", "fullUri");
		when(pluginRegistry.getPlugin("plugin0")).thenReturn(molgenisPlugin0);
		org.molgenis.data.plugin.Plugin molgenisPlugin1 = new org.molgenis.data.plugin.Plugin("plugin1", "Plugin #1",
				"uri", "fullUri");
		when(pluginRegistry.getPlugin("plugin1")).thenReturn(molgenisPlugin1);
		when(pluginRegistry.getPlugins()).thenReturn(asList(molgenisPlugin0, molgenisPlugin1));

		Plugin plugin0 = mock(Plugin.class);
		when(plugin0.getId()).thenReturn("plugin0");
		when(plugin0.getLabel()).thenReturn("Plugin #0");
		when(plugin0.getUri()).thenReturn("uri");
		when(plugin0.getFullUri()).thenReturn("fullUri");

		Plugin plugin1 = mock(Plugin.class);
		when(plugin1.getId()).thenReturn("plugin1");
		when(plugin1.getLabel()).thenReturn("Plugin #1");
		when(plugin1.getUri()).thenReturn("uri");
		when(plugin1.getFullUri()).thenReturn("fullUri");

		when(pluginFactory.create("plugin0")).thenReturn(plugin0);
		when(pluginFactory.create("plugin1")).thenReturn(plugin1);
		when(dataService.findAll(PluginMetadata.PLUGIN, Plugin.class)).thenReturn(Stream.of(plugin0, plugin1));

		pluginPopulator.populate();

		verify(dataService).findAll(PluginMetadata.PLUGIN, Plugin.class);
		verify(dataService).getRepository(PluginMetadata.PLUGIN, Plugin.class);
		verifyNoMoreInteractions(dataService);
		verify(pluginRepository).upsertBatch(asList(plugin0, plugin1));
	}
}