package org.molgenis.mutationdb;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.mysql.MysqlRepository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.mutationdb.MutationsViewControllerTest.Config;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = Config.class)
public class MutationsViewControllerTest extends AbstractTestNGSpringContextTests
{	
	@Autowired
	public MysqlViewService mysqlViewService;

	@Autowired
	public DataService dataService;

	@Autowired
	public DataSource dataSource;

	@Autowired
	public MutationsViewController mutationsViewController;

	private MockMvc mockMvc;

	@BeforeMethod
	public void beforeMethod()
	{
		mockMvc = MockMvcBuilders.standaloneSetup(mutationsViewController)
				.setMessageConverters(new GsonHttpMessageConverter()).build();
	}

	@Test
	public void init() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.get(MutationsViewController.URI))
				.andExpect(MockMvcResultMatchers.status().isOk())
				.andExpect(view().name("view-col7a1")).andExpect(model().attributeExists("title"));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void mutationsViewControllerWithoutDataService()
	{
		new MutationsViewController(null, mysqlViewService);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void mutationsViewControllerWithoutMysqlViewService()
	{
		new MutationsViewController(dataService, null);
	}

	@Test
	public void refreshReturnTrue() throws Exception
	{
		MysqlRepository mutationsViewRepo = mock(MysqlRepository.class);

		when(dataService.hasRepository(MutationsViewController.ENTITYNAME_MUTATIONSVIEW)).thenReturn(true);
		when(dataService.getRepositoryByEntityName(MutationsViewController.ENTITYNAME_MUTATIONSVIEW))
				.thenReturn(mutationsViewRepo);

		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(mutationsViewRepo.getEntityMetaData()).thenReturn(entityMetaData);
		when(entityMetaData.getName()).thenReturn(MutationsViewController.ENTITYNAME_MUTATIONSVIEW);

		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(MutationsViewController.URI + "/generate"))
				.andExpect(status().isOk()).andReturn();
		boolean content = Boolean.valueOf(result.getResponse().getContentAsString());
		assertTrue(content);
	}

	@Test
	public void refreshReturnFalse() throws Exception
	{
		when(dataService.hasRepository(MutationsViewController.ENTITYNAME_MUTATIONSVIEW)).thenReturn(false);
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(MutationsViewController.URI + "/generate"))
				.andExpect(status().isOk()).andReturn();
		boolean content = Boolean.valueOf(result.getResponse().getContentAsString());
		assertFalse(content);
	}

	@Test
	public void create() throws Exception
	{
		MysqlRepository mutationsViewRepo = mock(MysqlRepository.class);
		MysqlRepository mutaionRepo = mock(MysqlRepository.class);
		
		Entity entity = mock(Entity.class);
		when(entity.getString(MutationsViewController.MUTATIONS__MUTATION_ID)).thenReturn("M1");
		List<Entity> entities = Arrays.asList(entity);
		when(mutaionRepo.iterator()).thenReturn(entities.iterator());
		
		when(mutationsViewRepo.findAll(new QueryImpl().eq(MutationsViewController.MUTATIONS__MUTATION_ID, "M1")))
				.thenReturn(entities);
		
		Map<String, List<Value>> valuesPerHeader = new HashMap<String, List<Value>>();
		when(this.mysqlViewService.valuesPerHeader(MutationsViewController.HEADERS_NAMES, entities)).thenReturn(
				valuesPerHeader);
		
		when(
				this.mysqlViewService.createRowByMergingValuesIfEquales(MutationsViewController.HEADERS_NAMES,
						valuesPerHeader)).thenReturn(new Row());
		
		when((MysqlRepository) dataService.getRepositoryByEntityName(MutationsViewController.ENTITYNAME_MUTATIONSVIEW))
				.thenReturn(mutationsViewRepo);
				
		when((MysqlRepository) dataService.getRepositoryByEntityName(MutationsViewController.ENTITYNAME_MUTATIONS))
				.thenReturn(mutaionRepo);
		
		when(dataService.hasRepository(MutationsViewController.ENTITYNAME_MUTATIONS)).thenReturn(true);
		when(dataService.hasRepository(MutationsViewController.ENTITYNAME_MUTATIONSVIEW)).thenReturn(true);
		
		mockMvc.perform(MockMvcRequestBuilders.get(MutationsViewController.URI + "/create")).andExpect(status().isOk())
				.andExpect(view().name("view-col7a1-table")).andExpect(model().attributeExists("rows"))
				.andExpect(model().attributeExists("headers"));
	}

	@Configuration
	public static class Config
	{
		@Bean
		public MysqlViewService mysqlViewService()
		{
			return mock(MysqlViewService.class);
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}
		
		@Bean
		public DataSource dataSource()
		{
			return mock(DataSource.class);
		}

		@Bean
		public MutationsViewController mutationsViewController()
		{
			return new MutationsViewController(dataService(), mysqlViewService());
		}
	}
}
