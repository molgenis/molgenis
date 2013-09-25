package org.molgenis.omx.controller;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.molgenis.framework.db.Database;
import org.molgenis.omx.controller.DataSetsIndexerController.DataSetIndexRequest;
import org.molgenis.omx.controller.DataSetsIndexerControllerTest.Config;
import org.molgenis.omx.search.DataSetsIndexer;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;

@WebAppConfiguration
@ContextConfiguration(classes = Config.class)
public class DataSetsIndexerControllerTest extends AbstractTestNGSpringContextTests
{

	@Autowired
	private DataSetsIndexerController dataSetsIndexerController;

	private MockMvc mockMvc;

	@BeforeMethod
	public void setUp()
	{
		mockMvc = MockMvcBuilders.standaloneSetup(dataSetsIndexerController)
				.setMessageConverters(new GsonHttpMessageConverter()).build();
	}

	@Test
	public void index() throws Exception
	{
		mockMvc.perform(
				post(DataSetsIndexerController.URI + "/index", "json").contentType(MediaType.APPLICATION_JSON).content(
						new Gson().toJson(new DataSetIndexRequest(Arrays.asList("1", "2"))).getBytes())).andExpect(
				status().isOk());
	}

	@Test
	public void init() throws Exception
	{
		mockMvc.perform(get(DataSetsIndexerController.URI)).andExpect(status().isOk());
	}

	@Configuration
	public static class Config
	{
		@Bean
		public DataSetsIndexerController dataSetsIndexerController()
		{
			return new DataSetsIndexerController();
		}

		@Bean
		public Database database()
		{
			return mock(Database.class);
		}

		@Bean
		public DataSetsIndexer getDataSetsIndexer()
		{
			DataSetsIndexer dataSetsIndexer = mock(DataSetsIndexer.class);
			when(dataSetsIndexer.isIndexingRunning()).thenReturn(true);
			return dataSetsIndexer;
		}
	}
}
