package org.molgenis.data.annotation.core.resources.impl;

import com.google.common.collect.ImmutableMap;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.Repository;
import org.molgenis.data.annotation.core.resources.MultiResourceConfig;
import org.molgenis.data.annotation.core.resources.ResourceConfig;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.test.AbstractMockitoTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class MultiFileResourceTest extends AbstractMockitoTest
{
	private MultiFileResource multiFileResource;
	@Mock
	private MultiResourceConfig config;
	@Mock
	private RepositoryFactory factory;
	private Map<String, ResourceConfig> configs;
	@Mock
	private ResourceConfig chrom3Config;
	@Mock
	private ResourceConfig chrom4Config;
	@Mock
	private Repository<Entity> chrom3Repository;
	@Mock
	private Repository<Entity> chrom4Repository;

	public MultiFileResourceTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		configs = ImmutableMap.of("3", chrom3Config, "4", chrom4Config);
		when(config.getConfigs()).thenReturn(configs);
		multiFileResource = new MultiFileResource("name", config)
		{
			@Override
			public RepositoryFactory getRepositoryFactory()
			{
				return factory;
			}
		};
	}

	@Test
	public void findAllDelegatesToTheCorrectResource() throws IOException
	{
		File chrom3File = File.createTempFile("chrom3", "tmp");
		when(chrom3Config.getFile()).thenReturn(chrom3File);
		when(factory.createRepository(chrom3File)).thenReturn(chrom3Repository);
		Query<Entity> q = QueryImpl.EQ("#CHROM", "3").and().eq("POS", 12345);
		Entity entity = mock(Entity.class);
		when(chrom3Repository.findAll(q)).thenReturn(Stream.of(entity));
		assertEquals(newArrayList(multiFileResource.findAll(q)), Collections.singletonList(entity));
		verify(chrom3Repository).findAll(q);
	}

	@Test
	public void findAllNoResourceForChrom() throws IOException
	{
		Query<Entity> q = QueryImpl.EQ("#CHROM", "MT").and().eq("POS", 12345);
		assertEquals(newArrayList(multiFileResource.findAll(q)), Collections.emptyList());
	}

	@Test
	public void whenAllFilesAvailableThenIsAvailableReturnsTrue() throws IOException
	{
		File chrom3File = File.createTempFile("chrom3", "tmp");
		when(chrom3Config.getFile()).thenReturn(chrom3File);
		File chrom4File = File.createTempFile("chrom4", "tmp");
		when(chrom4Config.getFile()).thenReturn(chrom4File);

		when(factory.createRepository(chrom3File)).thenReturn(chrom3Repository);
		when(factory.createRepository(chrom4File)).thenReturn(chrom4Repository);

		assertTrue(multiFileResource.isAvailable());
	}

	@Test
	public void whenOneFileIsNotAvailableThenIsAvailableReturnsFalse() throws IOException
	{
		File chrom3File = File.createTempFile("chrom3", "tmp");
		when(chrom3Config.getFile()).thenReturn(chrom3File);
		File chrom4File = new File("bogusChrom4");
		when(chrom4Config.getFile()).thenReturn(chrom4File);
		when(factory.createRepository(chrom3File)).thenReturn(chrom3Repository);
		assertFalse(multiFileResource.isAvailable());
	}

	@Test
	public void whenOneConfigChangesFilePatternThenIsAvailableUpdates() throws IOException
	{
		File chrom3File = File.createTempFile("chrom3", "tmp");
		when(chrom3Config.getFile()).thenReturn(chrom3File);
		File chrom4File = File.createTempFile("chrom4", "tmp");
		when(chrom4Config.getFile()).thenReturn(chrom4File);

		when(factory.createRepository(chrom3File)).thenReturn(chrom3Repository);
		when(factory.createRepository(chrom4File)).thenReturn(chrom4Repository);
		assertTrue(multiFileResource.isAvailable());

		when(chrom3Config.getFile()).thenReturn(new File("bogus"));
		assertFalse(multiFileResource.isAvailable());

		when(chrom3Config.getFile()).thenReturn(chrom3File);
		assertTrue(multiFileResource.isAvailable());

		when(chrom4Config.getFile()).thenReturn(new File("bogus"));
		assertFalse(multiFileResource.isAvailable());
	}

	@Test
	public void whenConfigsChangeThenIsAvailableUpdates() throws IOException
	{
		SingleResourceConfig chrom5Config = mock(SingleResourceConfig.class);
		when(config.getConfigs()).thenReturn(ImmutableMap.of("5", chrom5Config));

		File chrom5File = new File("Bogus");
		when(chrom5Config.getFile()).thenReturn(chrom5File);

		assertFalse(multiFileResource.isAvailable());

		when(chrom5Config.getFile()).thenReturn(File.createTempFile("chrom5", "tmp"));
		assertTrue(multiFileResource.isAvailable());
	}
}
