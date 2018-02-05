package org.molgenis.data.plugin.model;

import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.molgenis.data.Repository;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.security.acls.model.MutableAclService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class PluginSecurityRepositoryDecoratorTest extends AbstractMockitoTest
{
	@Mock
	private Repository<Plugin> delegateRepository;

	@Mock
	private MutableAclService mutableAclService;

	private PluginSecurityRepositoryDecorator pluginSecurityRepositoryDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		pluginSecurityRepositoryDecorator = new PluginSecurityRepositoryDecorator(delegateRepository,
				mutableAclService);
	}

	@Test(expectedExceptions = NullPointerException.class)
	public void testPluginSecurityRepositoryDecorator()
	{
		new PluginSecurityRepositoryDecorator(null, null);
	}

	@Test
	public void testAdd()
	{
		Plugin plugin = when(mock(Plugin.class).getId()).thenReturn("plugin0").getMock();
		pluginSecurityRepositoryDecorator.add(plugin);
		verify(mutableAclService).createAcl(new PluginIdentity("plugin0"));
		verify(delegateRepository).add(plugin);
	}

	@Test
	public void testAddStream()
	{
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
	public void testDelete()
	{
		Plugin plugin = when(mock(Plugin.class).getId()).thenReturn("plugin0").getMock();
		pluginSecurityRepositoryDecorator.delete(plugin);
		verify(mutableAclService).deleteAcl(new PluginIdentity("plugin0"), true);
		verify(delegateRepository).delete(plugin);
	}

	@Test
	public void testDeleteStream()
	{
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
	public void testDeleteById()
	{
		pluginSecurityRepositoryDecorator.deleteById("plugin0");
		verify(mutableAclService).deleteAcl(new PluginIdentity("plugin0"), true);
		verify(delegateRepository).deleteById("plugin0");
	}

	@Test
	public void testDeleteAll()
	{
		Plugin plugin0 = when(mock(Plugin.class).getId()).thenReturn("plugin0").getMock();
		Plugin plugin1 = when(mock(Plugin.class).getId()).thenReturn("plugin1").getMock();
		when(delegateRepository.iterator()).thenReturn(Stream.of(plugin0, plugin1).iterator());
		pluginSecurityRepositoryDecorator.deleteAll();
		verify(delegateRepository).deleteAll();
		verify(mutableAclService).deleteAcl(new PluginIdentity("plugin0"), true);
		verify(mutableAclService).deleteAcl(new PluginIdentity("plugin1"), true);
	}

	@Test
	public void testDeleteAllStream()
	{
		pluginSecurityRepositoryDecorator.deleteAll(Stream.of("plugin0", "plugin1"));

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Object>> pluginCaptor = ArgumentCaptor.forClass(Stream.class);
		verify(delegateRepository).deleteAll(pluginCaptor.capture());
		assertEquals(pluginCaptor.getValue().collect(toList()), asList("plugin0", "plugin1"));
		verify(mutableAclService).deleteAcl(new PluginIdentity("plugin0"), true);
		verify(mutableAclService).deleteAcl(new PluginIdentity("plugin1"), true);
	}
}