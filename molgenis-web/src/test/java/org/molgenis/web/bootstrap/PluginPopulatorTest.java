package org.molgenis.web.bootstrap;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.stream.Stream;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
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

public class PluginPopulatorTest extends AbstractMockitoTest {
  @Mock private DataService dataService;
  @Mock private PluginFactory pluginFactory;
  @Captor private ArgumentCaptor<List<Plugin>> updateStreamArgumentCaptor;
  @Captor private ArgumentCaptor<Stream<Plugin>> deleteStreamArgumentCaptor;
  @Mock private Repository<Plugin> pluginRepository;

  private static final String NEW_PLUGIN = "newPlugin";
  private static final String CHANGED_PLUGIN = "changedPlugin";
  private static final String DELETED_PLUGIN = "deletedPlugin";
  private static final String EXISTING_APP_PLUGIN = "app-existingAppPlugin";

  private PluginPopulator pluginPopulator;

  public PluginPopulatorTest() {
    super(Strictness.WARN);
  }

  @BeforeMethod
  public void setUpBeforeMethod() {
    pluginPopulator = new PluginPopulator(dataService, pluginFactory);
  }

  @Test
  public void testPopulate() {
    PluginController pluginController0 =
        when(mock(PluginController.class).getId()).thenReturn(NEW_PLUGIN).getMock();
    PluginController pluginController1 =
        when(mock(PluginController.class).getId()).thenReturn(CHANGED_PLUGIN).getMock();

    ApplicationContext ctx = mock(ApplicationContext.class);
    when(ctx.getBeansOfType(PluginController.class))
        .thenReturn(
            ImmutableMap.of(NEW_PLUGIN, pluginController0, CHANGED_PLUGIN, pluginController1));

    Plugin newPlugin = when(mock(Plugin.class).getId()).thenReturn(NEW_PLUGIN).getMock();
    when(newPlugin.setLabel(NEW_PLUGIN)).thenReturn(newPlugin);
    when(newPlugin.setPath(NEW_PLUGIN)).thenReturn(newPlugin);

    Plugin changedPlugin = when(mock(Plugin.class).getId()).thenReturn(CHANGED_PLUGIN).getMock();
    when(changedPlugin.setLabel(CHANGED_PLUGIN)).thenReturn(changedPlugin);
    when(changedPlugin.setPath(CHANGED_PLUGIN)).thenReturn(changedPlugin);

    Plugin existingChangedPlugin =
        when(mock(Plugin.class).getId()).thenReturn(CHANGED_PLUGIN).getMock();
    Plugin deletedPlugin = when(mock(Plugin.class).getId()).thenReturn(DELETED_PLUGIN).getMock();

    when(pluginFactory.create(NEW_PLUGIN)).thenReturn(newPlugin);
    when(pluginFactory.create(CHANGED_PLUGIN)).thenReturn(changedPlugin);
    when(dataService.getRepository(PluginMetadata.PLUGIN, Plugin.class))
        .thenReturn(pluginRepository);
    when(dataService.findAll(PluginMetadata.PLUGIN, Plugin.class))
        .thenReturn(Stream.of(existingChangedPlugin, deletedPlugin));

    pluginPopulator.populate(ctx);

    verify(pluginRepository).upsertBatch(updateStreamArgumentCaptor.capture());
    assertEquals(
        newHashSet(updateStreamArgumentCaptor.getValue()), newHashSet(newPlugin, changedPlugin));
    verify(dataService).delete(eq(PluginMetadata.PLUGIN), deleteStreamArgumentCaptor.capture());
    assertEquals(
        deleteStreamArgumentCaptor.getValue().collect(toList()), singletonList(deletedPlugin));
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testPopulateDoNotRemoveExistingAppPlugin() {
    Plugin deletedPlugin = when(mock(Plugin.class).getId()).thenReturn(DELETED_PLUGIN).getMock();
    Plugin existingAppPlugin =
        when(mock(Plugin.class).getId()).thenReturn(EXISTING_APP_PLUGIN).getMock();

    when(dataService.findAll(PluginMetadata.PLUGIN, Plugin.class))
        .thenReturn(Stream.of(existingAppPlugin, deletedPlugin));

    pluginPopulator.populate(mock(ApplicationContext.class));

    verify(pluginRepository, times(0)).upsertBatch(any(List.class));
    verify(dataService).delete(eq(PluginMetadata.PLUGIN), deleteStreamArgumentCaptor.capture());
    assertEquals(
        deleteStreamArgumentCaptor.getValue().collect(toList()), newArrayList(deletedPlugin));
  }
}
