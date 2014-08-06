package org.molgenis.mutationdb;

import static org.mockito.Mockito.doNothing;
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
import org.molgenis.framework.ui.MolgenisPluginRegistry;
import org.molgenis.framework.ui.MolgenisPluginRegistryImpl;
import org.molgenis.mutationdb.PatientsViewControllerTest.Config;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.MySqlFileUtil;
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
public class PatientsViewControllerTest extends AbstractTestNGSpringContextTests
{
	@Autowired
	public MysqlViewService mysqlViewService;

	@Autowired
	public DataService dataService;

	@Autowired
	public DataSource dataSource;

	@Autowired
	public PatientsViewController patientsViewController;

	private MockMvc mockMvc;

	@BeforeMethod
	public void beforeMethod()
	{
		mockMvc = MockMvcBuilders.standaloneSetup(patientsViewController)
				.setMessageConverters(new GsonHttpMessageConverter()).build();
	}

	@Test
	public void init() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.get(PatientsViewController.URI))
				.andExpect(MockMvcResultMatchers.status().isOk()).andExpect(view().name("view-col7a1"))
				.andExpect(model().attributeExists("title"));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void patientsViewControllerWithoutDataService()
	{
		new PatientsViewController(null, mysqlViewService);
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void patientsViewControllerWithoutMysqlViewService()
	{
		new PatientsViewController(dataService, null);
	}

	@Test
	public void refreshReturnTrue() throws Exception
	{
		MysqlRepository patientsViewRepo = mock(MysqlRepository.class);

		when(dataService.hasRepository(PatientsViewController.ENTITYNAME_PATIENTSVIEW)).thenReturn(true);
		doNothing().when(mysqlViewService).truncate(PatientsViewController.ENTITYNAME_PATIENTSVIEW);
		doNothing().when(mysqlViewService).populateWithQuery(
				MySqlFileUtil.getMySqlQueryFromFile(PatientsViewController.class,
						PatientsViewController.PATH_TO_INSERT_QUERY));
		when(dataService.getRepositoryByEntityName(PatientsViewController.ENTITYNAME_PATIENTSVIEW)).thenReturn(
				patientsViewRepo).getMock();

		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(patientsViewRepo.getEntityMetaData()).thenReturn(entityMetaData);
		when(entityMetaData.getName()).thenReturn(PatientsViewController.ENTITYNAME_PATIENTSVIEW);
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(PatientsViewController.URI + "/generate"))
				.andExpect(status().isOk()).andReturn();
		boolean content = Boolean.valueOf(result.getResponse().getContentAsString());
		assertTrue(content);
	}

	@Test
	public void refreshReturnFalse() throws Exception
	{
		when(dataService.hasRepository(PatientsViewController.ENTITYNAME_PATIENTSVIEW)).thenReturn(false);
		MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get(PatientsViewController.URI + "/generate"))
				.andExpect(status().isOk()).andReturn();
		boolean content = Boolean.valueOf(result.getResponse().getContentAsString());
		assertFalse(content);
	}

	@Test
	public void create() throws Exception
	{
		MysqlRepository patientsViewRepo = mock(MysqlRepository.class);
		MysqlRepository patientsRepo = mock(MysqlRepository.class);

		Entity entity = mock(Entity.class);
		when(entity.getString(PatientsViewController.PATIENT_ID)).thenReturn("P1");
		List<Entity> entities = Arrays.asList(entity);
		when(patientsRepo.iterator()).thenReturn(entities.iterator());

		when(patientsViewRepo.findAll(new QueryImpl().eq(PatientsViewController.PATIENT_ID, "P1")))
				.thenReturn(entities);

		Map<String, List<Value>> valuesPerHeader = new HashMap<String, List<Value>>();
		when(this.mysqlViewService.valuesPerHeader(PatientsViewController.HEADERS_NAMES, entities)).thenReturn(
				valuesPerHeader);

		when(
				this.mysqlViewService.createRowByMergingValuesIfEquales(PatientsViewController.HEADERS_NAMES,
						valuesPerHeader)).thenReturn(new Row());

		when((MysqlRepository) dataService.getRepositoryByEntityName(PatientsViewController.ENTITYNAME_PATIENTSVIEW))
				.thenReturn(patientsViewRepo);

		when((MysqlRepository) dataService.getRepositoryByEntityName(PatientsViewController.ENTITYNAME_PATIENTS))
				.thenReturn(patientsRepo);

		when(dataService.hasRepository(PatientsViewController.ENTITYNAME_PATIENTS)).thenReturn(true);
		when(dataService.hasRepository(PatientsViewController.ENTITYNAME_PATIENTSVIEW)).thenReturn(true);

		mockMvc.perform(MockMvcRequestBuilders.get(PatientsViewController.URI + "/create")).andExpect(status().isOk())
				.andExpect(view().name("view-col7a1-table")).andExpect(model().attributeExists("rows"))
				.andExpect(model().attributeExists("headers"));
	}

	@Configuration
	public static class Config
	{
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
		public MolgenisPluginRegistry molgenisPluginRegistry()
		{
			return new MolgenisPluginRegistryImpl();
		}

		@Bean
		public PatientsViewController patientsViewController()
		{
			return new PatientsViewController(dataService(), mysqlViewService());
		}

		@Bean
		public MysqlViewService mysqlViewService()
		{
			return mock(MysqlViewService.class);
		}
	}
}
