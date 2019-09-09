package org.molgenis.data.plugin.model;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.EntityAlreadyExistsException;
import org.molgenis.data.Repository;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.model.AlreadyExistsException;
import org.springframework.security.acls.model.MutableAclService;

class PluginSecurityRepositoryDecoratorTest extends AbstractMockitoTest {
  @Mock private Repository<Plugin> delegateRepository;

  @Mock private MutableAclService mutableAclService;

  private PluginSecurityRepositoryDecorator pluginSecurityRepositoryDecorator;

  @BeforeEach
  void setUpBeforeMethod() {
    pluginSecurityRepositoryDecorator =
        new PluginSecurityRepositoryDecorator(delegateRepository, mutableAclService);
  }

  @Test
  void testPluginSecurityRepositoryDecorator() {
    assertThrows(
        NullPointerException.class, () -> new PluginSecurityRepositoryDecorator(null, null));
  }

  @Test
  void testAdd() {
    Plugin plugin = when(mock(Plugin.class).getId()).thenReturn("plugin0").getMock();
    pluginSecurityRepositoryDecorator.add(plugin);
    verify(mutableAclService).createAcl(new PluginIdentity("plugin0"));
    verify(delegateRepository).add(plugin);
  }

  @Test
  void testAddStream() {
    Plugin plugin0 = when(mock(Plugin.class).getId()).thenReturn("plugin0").getMock();
    Plugin plugin1 = when(mock(Plugin.class).getId()).thenReturn("plugin1").getMock();
    pluginSecurityRepositoryDecorator.add(Stream.of(plugin0, plugin1));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Plugin>> pluginCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).add(pluginCaptor.capture());
    assertEquals(pluginCaptor.getValue().collect(toList()), asList(plugin0, plugin1));
    verify(mutableAclService).createAcl(new PluginIdentity("plugin0"));
    verify(mutableAclService).createAcl(new PluginIdentity("plugin1"));
  }

  @Test
  void testAddAlreadyExists() {
    Plugin plugin = mock(Plugin.class);
    when(plugin.getId()).thenReturn("pluginId");
    when(plugin.getIdValue()).thenReturn("pluginId");
    PluginMetadata pluginMetadata =
        when(mock(PluginMetadata.class).getId()).thenReturn("Plugin").getMock();
    when(plugin.getEntityType()).thenReturn(pluginMetadata);
    when(mutableAclService.createAcl(new PluginIdentity(plugin)))
        .thenThrow(new AlreadyExistsException(""));
    Exception exception =
        assertThrows(
            EntityAlreadyExistsException.class,
            () -> pluginSecurityRepositoryDecorator.add(plugin));
    assertThat(exception.getMessage()).containsPattern("type:Plugin id:pluginId");
  }

  @Test
  void testDelete() {
    Plugin plugin = when(mock(Plugin.class).getId()).thenReturn("plugin0").getMock();
    pluginSecurityRepositoryDecorator.delete(plugin);
    verify(mutableAclService).deleteAcl(new PluginIdentity("plugin0"), true);
    verify(delegateRepository).delete(plugin);
  }

  @Test
  void testDeleteStream() {
    Plugin plugin0 = when(mock(Plugin.class).getId()).thenReturn("plugin0").getMock();
    Plugin plugin1 = when(mock(Plugin.class).getId()).thenReturn("plugin1").getMock();
    pluginSecurityRepositoryDecorator.delete(Stream.of(plugin0, plugin1));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Plugin>> pluginCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).delete(pluginCaptor.capture());
    assertEquals(pluginCaptor.getValue().collect(toList()), asList(plugin0, plugin1));
    verify(mutableAclService).deleteAcl(new PluginIdentity("plugin0"), true);
    verify(mutableAclService).deleteAcl(new PluginIdentity("plugin1"), true);
  }

  @Test
  void testDeleteById() {
    pluginSecurityRepositoryDecorator.deleteById("plugin0");
    verify(mutableAclService).deleteAcl(new PluginIdentity("plugin0"), true);
    verify(delegateRepository).deleteById("plugin0");
  }

  @Test
  void testDeleteAll() {
    Plugin plugin0 = when(mock(Plugin.class).getId()).thenReturn("plugin0").getMock();
    Plugin plugin1 = when(mock(Plugin.class).getId()).thenReturn("plugin1").getMock();
    when(delegateRepository.iterator()).thenReturn(Stream.of(plugin0, plugin1).iterator());
    pluginSecurityRepositoryDecorator.deleteAll();
    verify(delegateRepository).deleteAll();
    verify(mutableAclService).deleteAcl(new PluginIdentity("plugin0"), true);
    verify(mutableAclService).deleteAcl(new PluginIdentity("plugin1"), true);
  }

  @Test
  void testDeleteAllStream() {
    pluginSecurityRepositoryDecorator.deleteAll(Stream.of("plugin0", "plugin1"));

    @SuppressWarnings("unchecked")
    ArgumentCaptor<Stream<Object>> pluginCaptor = ArgumentCaptor.forClass(Stream.class);
    verify(delegateRepository).deleteAll(pluginCaptor.capture());
    assertEquals(pluginCaptor.getValue().collect(toList()), asList("plugin0", "plugin1"));
    verify(mutableAclService).deleteAcl(new PluginIdentity("plugin0"), true);
    verify(mutableAclService).deleteAcl(new PluginIdentity("plugin1"), true);
  }
}
