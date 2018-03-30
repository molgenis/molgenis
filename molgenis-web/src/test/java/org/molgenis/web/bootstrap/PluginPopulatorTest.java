package org.molgenis.web.bootstrap;

import com.google.common.collect.ImmutableMap;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Repository;
import org.molgenis.data.plugin.model.Plugin;
import org.molgenis.data.plugin.model.PluginFactory;
import org.molgenis.data.plugin.model.PluginMetadata;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.web.PluginController;
import org.springframework.context.ApplicationContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class PluginPopulatorTest extends AbstractMockitoTest
{
	@Mock
	private DataService dataService;
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
		pluginPopulator = new PluginPopulator(dataService, pluginFactory);
	}

	@Test
	public void testPopulate()
	{
		PluginController pluginController0 = when(mock(PluginController.class).getId()).thenReturn("newPlugin")
																					   .getMock();
		PluginController pluginController1 = when(mock(PluginController.class).getId()).thenReturn("changedPlugin")
																					   .getMock();

		ApplicationContext ctx = mock(ApplicationContext.class);
		when(ctx.getBeansOfType(PluginController.class)).thenReturn(
				ImmutableMap.of("newPlugin", pluginController0, "changedPlugin", pluginController1));

		Plugin newPlugin = when(mock(Plugin.class).getId()).thenReturn("newPlugin").getMock();
		when(newPlugin.setLabel("newPlugin")).thenReturn(newPlugin);
		Plugin changedPlugin = when(mock(Plugin.class).getId()).thenReturn("changedPlugin").getMock();
		when(changedPlugin.setLabel("changedPlugin")).thenReturn(changedPlugin);
		Plugin existingChangedPlugin = when(mock(Plugin.class).getId()).thenReturn("changedPlugin").getMock();
		Plugin deletedPlugin = when(mock(Plugin.class).getId()).thenReturn("deletedPlugin").getMock();

		doReturn(newPlugin).when(pluginFactory).create("newPlugin");
		doReturn(changedPlugin).when(pluginFactory).create("changedPlugin");
		when(dataService.getRepository(PluginMetadata.PLUGIN, Plugin.class)).thenReturn(pluginRepository);
		when(dataService.findAll(PluginMetadata.PLUGIN, Plugin.class)).thenReturn(
				Stream.of(existingChangedPlugin, deletedPlugin));

		pluginPopulator.populate(ctx);

		verify(pluginRepository).upsertBatch(updateStreamArgumentCaptor.capture());
		assertEquals(newHashSet(updateStreamArgumentCaptor.getValue()), newHashSet(newPlugin, changedPlugin));
		verify(dataService).delete(eq(PluginMetadata.PLUGIN), deleteStreamArgumentCaptor.capture());
		assertEquals(deleteStreamArgumentCaptor.getValue().collect(toList()), singletonList(deletedPlugin));
	}
}