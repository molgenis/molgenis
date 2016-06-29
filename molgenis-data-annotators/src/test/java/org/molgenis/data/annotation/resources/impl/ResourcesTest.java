package org.molgenis.data.annotation.resources.impl;

import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

//@ContextConfiguration(classes = ResourcesTest.Config.class)
public class ResourcesTest extends AbstractTestNGSpringContextTests
{
	//	@Autowired
	//	private Resource resource;
	//
	//	@Autowired
	//	private DataService dataService;
	//
	//	@Autowired
	//	private Resources resources;
	//
	//	private EntityMetaData emd;
	//
	//	private Entity e1;
	//
	//	@BeforeSuite
	//	public void beforeSuite()
	//	{
	//		emd = new EntityMetaData("resourceName");
	//		emd.addAttribute("id", ROLE_ID, ROLE_LABEL);
	//
	//		e1 = new DefaultEntity(emd, dataService);
	//		e1.set("id", "5");
	//	}
	//
	//	@BeforeMethod
	//	public void beforeMethod()
	//	{
	//		Mockito.reset(resource, dataService);
	//		when(resource.getName()).thenReturn("resourceName");
	//	}
	//
	//	@Test
	//	public void ifResourceExistsAndIsAvailableThenHasResourceReturnsTrue()
	//	{
	//		when(resource.isAvailable()).thenReturn(true);
	//		assertTrue(resources.hasRepository("resourceName"));
	//	}
	//
	//	@Test
	//	public void ifResourceExistsButIsUnavailableThenHasResourceReturnsTrue()
	//	{
	//		when(resource.isAvailable()).thenReturn(false);
	//		assertFalse(resources.hasRepository("resourceName"));
	//	}
	//
	//	@Test
	//	public void ifResourceDoesNotExistThenHasResourceReturnsFalse()
	//	{
	//		when(dataService.hasRepository("blah")).thenReturn(false);
	//		assertFalse(resources.hasRepository("blah"));
	//	}
	//
	//	@Test
	//	public void ifResourceExistsInDataServiceThenHasResourceReturnsTrue()
	//	{
	//		when(dataService.hasRepository("blah")).thenReturn(true);
	//		assertTrue(resources.hasRepository("blah"));
	//	}
	//
	//	@Test
	//	public void ifResourceExistsThenFindAllDelegatesWithoutCheckingAvailability()
	//	{
	//		Query<Entity> q = QueryImpl.EQ("id", "5");
	//		when(resource.findAll(q)).thenReturn(Arrays.asList(e1));
	//		assertEquals(resources.findAll("resourceName", q), Arrays.asList(e1));
	//		Mockito.verify(resource, Mockito.never()).isAvailable();
	//	}
	//
	//	@Test
	//	public void ifResourceExistsAndIsAvailableButQueryFailsThenExceptionGetsThrown()
	//	{
	//		Query<Entity> q = QueryImpl.EQ("id", "5");
	//		Exception ex = new RuntimeException();
	//		when(resource.findAll(q)).thenThrow(ex);
	//		when(resource.isAvailable()).thenReturn(true);
	//		try
	//		{
	//			resources.findAll("resourceName", q);
	//			fail("should throw exception");
	//		}
	//		catch (Exception expected)
	//		{
	//			assertSame(expected, ex);
	//		}
	//	}
	//
	//	@Test
	//	public void ifResourceIsUnavailableThenQueryIsDelegated()
	//	{
	//		Query<Entity> q = QueryImpl.EQ("id", "5");
	//		Exception ex = new RuntimeException();
	//		when(resource.findAll(q)).thenThrow(ex);
	//		when(resource.isAvailable()).thenReturn(false);
	//		when(dataService.findAll("resourceName", q)).thenReturn(Stream.of(e1));
	//		assertEquals(Lists.newArrayList(resources.findAll("resourceName", q)), Arrays.asList(e1));
	//	}
	//
	//	@Test
	//	public void ifResourceDoesNotExistThenQueryIsDelegated()
	//	{
	//		Query<Entity> q = QueryImpl.EQ("id", "5");
	//		when(dataService.findAll("blah", q)).thenReturn(Stream.of(e1));
	//		assertEquals(Lists.newArrayList(resources.findAll("blah", q)), Arrays.asList(e1));
	//	}
	//
	//	public static class Config
	//	{
	//		@Bean
	//		public Resource resource()
	//		{
	//			Resource result = mock(Resource.class);
	//			when(result.getName()).thenReturn("resourceName");
	//			return result;
	//		}
	//
	//		@Bean
	//		public DataService dataService()
	//		{
	//			return mock(DataService.class);
	//		}
	//
	//		@Bean
	//		public Resources resources()
	//		{
	//			return new ResourcesImpl();
	//		}
	//	}
}
