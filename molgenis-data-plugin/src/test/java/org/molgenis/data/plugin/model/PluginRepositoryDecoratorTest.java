package org.molgenis.data.plugin.model;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Repository;
import org.molgenis.settings.AppSettings;
import org.molgenis.test.AbstractMockitoTest;

class PluginRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock Repository<Plugin> delegateRepository;
  @Mock AppSettings appSettings;
  private PluginRepositoryDecorator pluginRepositoryDecorator;

  @BeforeEach
  void setUpBeforeMethod() {
    pluginRepositoryDecorator = new PluginRepositoryDecorator(delegateRepository, appSettings);
  }

  @Test
  void testPluginRepositoryDecorator() {
    assertThrows(NullPointerException.class, () -> new PluginRepositoryDecorator(null, null));
  }

  @Test
  void testDelete() {
    String pluginId = "myPluginId";
    Plugin plugin = when(mock(Plugin.class).getId()).thenReturn(pluginId).getMock();

    String menu =
        "{\"type\":\"menu\",\"id\":\"main\",\"items\":[{\"type\":\"plugin\",\"id\":\"myPluginId\"}]}";
    when(appSettings.getMenu()).thenReturn(menu);
    pluginRepositoryDecorator.delete(plugin);
    verify(delegateRepository).delete(plugin);
    verify(appSettings).setMenu("{\"type\":\"menu\",\"id\":\"main\",\"items\":[]}");
  }

  @Test
  void testDeleteNoEntries() {
    String pluginId = "myPluginId";
    Plugin plugin = when(mock(Plugin.class).getId()).thenReturn(pluginId).getMock();

    String menu =
        "{\"type\":\"menu\",\"id\":\"main\",\"items\":[{\"type\":\"plugin\",\"id\":\"myOtherPluginId\"}]}";
    when(appSettings.getMenu()).thenReturn(menu);
    pluginRepositoryDecorator.delete(plugin);
    verify(delegateRepository).delete(plugin);
    verify(appSettings)
        .setMenu(
            "{\"type\":\"menu\",\"id\":\"main\",\"items\":[{\"type\":\"plugin\",\"id\":\"myOtherPluginId\"}]}");
  }

  @Test
  void testDeleteMultipleMenuEntries() {
    String pluginId = "myPluginId";
    Plugin plugin = when(mock(Plugin.class).getId()).thenReturn(pluginId).getMock();

    String menu =
        "{\"type\":\"menu\",\"id\":\"main\",\"items\":[{\"type\":\"plugin\",\"id\":\"myPluginId\"}, {\"type\":\"menu\",\"id\":\"sub\",\"items\":[{\"type\":\"plugin\",\"id\":\"myPluginId\"}]}]}";
    when(appSettings.getMenu()).thenReturn(menu);
    pluginRepositoryDecorator.delete(plugin);
    verify(delegateRepository).delete(plugin);
    verify(appSettings)
        .setMenu(
            "{\"type\":\"menu\",\"id\":\"main\",\"items\":[{\"type\":\"menu\",\"id\":\"sub\",\"items\":[]}]}");
  }

  @Test
  void testDeleteById() {
    String pluginId = "myPluginId";

    String menu =
        "{\"type\":\"menu\",\"id\":\"main\",\"items\":[{\"type\":\"plugin\",\"id\":\"myPluginId\"}]}";
    when(appSettings.getMenu()).thenReturn(menu);
    pluginRepositoryDecorator.deleteById(pluginId);
    verify(delegateRepository).deleteById(pluginId);
    verify(appSettings).setMenu("{\"type\":\"menu\",\"id\":\"main\",\"items\":[]}");
  }

  @Test
  void testDeleteAll() {
    String pluginId = "myPluginId";
    Plugin plugin = when(mock(Plugin.class).getId()).thenReturn(pluginId).getMock();

    doAnswer(
            invocation -> {
              Consumer<List<Plugin>> consumer = invocation.getArgument(1);
              consumer.accept(singletonList(plugin));
              return null;
            })
        .when(delegateRepository)
        .forEachBatched(any(), any(), eq(1000));

    String menu =
        "{\"type\":\"menu\",\"id\":\"main\",\"items\":[{\"type\":\"plugin\",\"id\":\"myPluginId\"}]}";
    when(appSettings.getMenu()).thenReturn(menu);

    pluginRepositoryDecorator.deleteAll();

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Plugin>> pluginCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(pluginCaptor.capture());
    assertEquals(pluginCaptor.getValue().collect(toList()), singletonList(plugin));
    verify(appSettings).setMenu("{\"type\":\"menu\",\"id\":\"main\",\"items\":[]}");
  }

  @Test
  void testDeleteStream() {
    String pluginId = "myPluginId";
    Plugin plugin = when(mock(Plugin.class).getId()).thenReturn(pluginId).getMock();

    String menu =
        "{\"type\":\"menu\",\"id\":\"main\",\"items\":[{\"type\":\"plugin\",\"id\":\"myPluginId\"}]}";
    when(appSettings.getMenu()).thenReturn(menu);
    pluginRepositoryDecorator.delete(Stream.of(plugin));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Plugin>> pluginCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(pluginCaptor.capture());
    assertEquals(pluginCaptor.getValue().collect(toList()), singletonList(plugin));
    verify(appSettings).setMenu("{\"type\":\"menu\",\"id\":\"main\",\"items\":[]}");
  }

  @Test
  void testDeleteAllStream() {
    String pluginId = "myPluginId";

    String menu =
        "{\"type\":\"menu\",\"id\":\"main\",\"items\":[{\"type\":\"plugin\",\"id\":\"myPluginId\"}]}";
    when(appSettings.getMenu()).thenReturn(menu);
    pluginRepositoryDecorator.deleteAll(Stream.of(pluginId));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> pluginCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(pluginCaptor.capture());
    assertEquals(pluginCaptor.getValue().collect(toList()), singletonList(pluginId));
    verify(appSettings).setMenu("{\"type\":\"menu\",\"id\":\"main\",\"items\":[]}");
  }
}
