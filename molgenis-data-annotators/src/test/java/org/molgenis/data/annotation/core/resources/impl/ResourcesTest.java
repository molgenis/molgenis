package org.molgenis.data.annotation.core.resources.impl;

import com.google.common.collect.Lists;
import org.mockito.Mockito;
import org.molgenis.data.AbstractMolgenisSpringTest;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.config.EffectsTestConfig;
import org.molgenis.data.annotation.core.resources.Resource;
import org.molgenis.data.annotation.core.resources.Resources;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.vcf.config.VcfTestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

@ContextConfiguration(classes = ResourcesTest.Config.class)
public class ResourcesTest extends AbstractMolgenisSpringTest
{
	@Autowired
	AttributeFactory attributeFactory;

	@Autowired
	EntityTypeFactory entityTypeFactory;

	@Autowired
	private Resource resource;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	private EntityType emd;

	private Entity e1;

	@BeforeClass
	public void beforeClass()
	{
		emd = entityTypeFactory.create("resourceName");
		Attribute attr = attributeFactory.create().setName("id").setIdAttribute(true).setLabelAttribute(true);
		emd.addAttribute(attr);

		e1 = new DynamicEntity(emd);
		e1.set("id", "5");
	}

	@BeforeMethod
	public void beforeMethod()
	{
		Mockito.reset(resource, dataService);
		when(resource.getName()).thenReturn("resourceName");
	}

	@Test
	public void ifResourceExistsAndIsAvailableThenHasResourceReturnsTrue()
	{
		when(resource.isAvailable()).thenReturn(true);
		assertTrue(resources.hasRepository("resourceName"));
	}

	@Test
	public void ifResourceExistsButIsUnavailableThenHasResourceReturnsTrue()
	{
		when(resource.isAvailable()).thenReturn(false);
		assertFalse(resources.hasRepository("resourceName"));
	}

	@Test
	public void ifResourceDoesNotExistThenHasResourceReturnsFalse()
	{
		when(dataService.hasRepository("blah")).thenReturn(false);
		assertFalse(resources.hasRepository("blah"));
	}

	@Test
	public void ifResourceExistsInDataServiceThenHasResourceReturnsTrue()
	{
		when(dataService.hasRepository("blah")).thenReturn(true);
		assertTrue(resources.hasRepository("blah"));
	}

	@Test
	public void ifResourceExistsThenFindAllDelegatesWithoutCheckingAvailability()
	{
		Query<Entity> q = QueryImpl.EQ("id", "5");
		when(resource.findAll(q)).thenReturn(Arrays.asList(e1));
		assertEquals(resources.findAll("resourceName", q), Arrays.asList(e1));
		Mockito.verify(resource, Mockito.never()).isAvailable();
	}

	@Test
	public void ifResourceExistsAndIsAvailableButQueryFailsThenExceptionGetsThrown()
	{
		Query<Entity> q = QueryImpl.EQ("id", "5");
		Exception ex = new RuntimeException();
		when(resource.findAll(q)).thenThrow(ex);
		when(resource.isAvailable()).thenReturn(true);
		try
		{
			resources.findAll("resourceName", q);
			fail("should throw exception");
		}
		catch (Exception expected)
		{
			assertSame(expected, ex);
		}
	}

	@Test
	public void ifResourceIsUnavailableThenQueryIsDelegated()
	{
		Query<Entity> q = QueryImpl.EQ("id", "5");
		Exception ex = new RuntimeException();
		when(resource.findAll(q)).thenThrow(ex);
		when(resource.isAvailable()).thenReturn(false);
		when(dataService.findAll("resourceName", q)).thenReturn(Stream.of(e1));
		assertEquals(Lists.newArrayList(resources.findAll("resourceName", q)), Arrays.asList(e1));
	}

	@Test
	public void ifResourceDoesNotExistThenQueryIsDelegated()
	{
		Query<Entity> q = QueryImpl.EQ("id", "5");
		when(dataService.findAll("blah", q)).thenReturn(Stream.of(e1));
		assertEquals(Lists.newArrayList(resources.findAll("blah", q)), Arrays.asList(e1));
	}

	@Configuration
	@Import({ VcfTestConfig.class, EffectsTestConfig.class })
	public static class Config
	{
		@Bean
		public Resource resource()
		{
			Resource result = mock(Resource.class);
			when(result.getName()).thenReturn("resourceName");
			return result;
		}

		@Bean
		public Resources resources()
		{
			return new ResourcesImpl();
		}
	}
}
