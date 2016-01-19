package org.molgenis.data.annotation.resources.impl;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.Arrays;
import java.util.stream.Stream;

import org.mockito.Mockito;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.annotation.resources.Resource;
import org.molgenis.data.annotation.resources.Resources;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

@ContextConfiguration(classes = ResourcesTest.Config.class)
public class ResourcesTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	private Resource resource;

	@Autowired
	private DataService dataService;

	@Autowired
	private Resources resources;

	private DefaultEntityMetaData emd;

	private Entity e1;

	@BeforeSuite
	public void beforeSuite()
	{
		emd = new DefaultEntityMetaData("resourceName");
		emd.addAttribute("id").setIdAttribute(true).setLabelAttribute(true);

		e1 = new DefaultEntity(emd, dataService);
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
		Query q = QueryImpl.EQ("id", "5");
		when(resource.findAll(q)).thenReturn(Arrays.asList(e1));
		assertEquals(resources.findAll("resourceName", q), Arrays.asList(e1));
		Mockito.verify(resource, Mockito.never()).isAvailable();
	}

	@Test
	public void ifResourceExistsAndIsAvailableButQueryFailsThenExceptionGetsThrown()
	{
		Query q = QueryImpl.EQ("id", "5");
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
		Query q = QueryImpl.EQ("id", "5");
		Exception ex = new RuntimeException();
		when(resource.findAll(q)).thenThrow(ex);
		when(resource.isAvailable()).thenReturn(false);
		when(dataService.findAll("resourceName", q)).thenReturn(Stream.of(e1));
		assertEquals(Lists.newArrayList(resources.findAll("resourceName", q)), Arrays.asList(e1));
	}

	@Test
	public void ifResourceDoesNotExistThenQueryIsDelegated()
	{
		Query q = QueryImpl.EQ("id", "5");
		when(dataService.findAll("blah", q)).thenReturn(Stream.of(e1));
		assertEquals(Lists.newArrayList(resources.findAll("blah", q)), Arrays.asList(e1));
	}

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
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public Resources resources()
		{
			return new ResourcesImpl();
		}
	}
}
