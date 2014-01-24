package org.molgenis.data.rest;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;
import static org.molgenis.data.rest.RestController.BASE_URI;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.Query;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.Updateable;
import org.molgenis.data.rest.RestControllerTest.RestControllerConfig;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = RestControllerConfig.class)
public class RestControllerTest extends AbstractTestNGSpringContextTests
{
	private static String PERSON = "Person";

	@Autowired
	private RestController restController;

	@Autowired
	private DataService dataService;

	private MockMvc mockMvc;

	private Entity newEntity;

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@BeforeMethod
	public void beforeMethod()
	{
		reset(dataService);

		Repository repo = mock(Repository.class, withSettings().extraInterfaces(Updateable.class, Queryable.class));

		Entity entity = new MapEntity("id");
		entity.set("id", 1);
		entity.set("name", "Piet");

		when(dataService.getEntityNames()).thenReturn(Arrays.asList(PERSON));
		when(dataService.getRepositoryByEntityName(PERSON)).thenReturn(repo);

		newEntity = new MapEntity();
		newEntity.set("name", "Piet");
		newEntity.set("id", null);

		when(dataService.add(PERSON, newEntity)).thenReturn(1);
		when(dataService.findOne(PERSON, 1)).thenReturn(entity);

		Query q = new QueryImpl().eq("name", "Piet").pageSize(10).offset(5);
		when(dataService.findAll(PERSON, q)).thenReturn(Arrays.asList(entity));

		AttributeMetaData attrName = new DefaultAttributeMetaData("name", FieldTypeEnum.STRING);
		DefaultAttributeMetaData attrId = new DefaultAttributeMetaData("id", FieldTypeEnum.INT);
		attrId.setIdAttribute(true);
		attrId.setVisible(false);

		when(repo.getAttribute("name")).thenReturn(attrName);
		when(repo.getIdAttribute()).thenReturn(attrId);
		when(repo.getAttributes()).thenReturn(Arrays.<AttributeMetaData> asList(attrName, attrId));
		when(repo.getName()).thenReturn(PERSON);

		mockMvc = MockMvcBuilders.standaloneSetup(restController).setMessageConverters(new GsonHttpMessageConverter())
				.build();
	}

	@Test
	public void create() throws Exception
	{
		mockMvc.perform(post(BASE_URI + "/person").content("{name:Piet}").contentType(APPLICATION_JSON))
				.andExpect(status().isCreated()).andExpect(header().string("Location", BASE_URI + "/person/1"));

		verify(dataService).add(PERSON, newEntity);
	}

	@Test
	public void createFromFormPost() throws Exception
	{
		mockMvc.perform(post(BASE_URI + "/person").contentType(APPLICATION_FORM_URLENCODED).param("name", "Piet"))
				.andExpect(status().isCreated()).andExpect(header().string("Location", BASE_URI + "/person/1"));

		verify(dataService).add(PERSON, newEntity);
	}

	@Test
	public void deleteDelete() throws Exception
	{
		mockMvc.perform(delete(BASE_URI + "/person/1")).andExpect(status().isNoContent());
		verify(dataService).delete(PERSON, 1);
	}

	@Test
	public void deletePost() throws Exception
	{
		mockMvc.perform(post(BASE_URI + "/person/1?_method=DELETE")).andExpect(status().isNoContent());
		verify(dataService).delete(PERSON, 1);
	}

	@Test
	public void getMetaData() throws Exception
	{
		mockMvc.perform(get(BASE_URI + "/person/meta"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(
						content()
								.string("{\"name\":\"Person\",\"attributes\":{\"name\":{\"name\":\"name\",\"fieldType\":\"STRING\",\"nillable\":true,\"readOnly\":false,\"idAttribute\":false,\"labelAttribute\":false,\"label\":\"name\",\"visible\":true,\"unique\":false}}}"));

	}

	@Test
	public void retrieve() throws Exception
	{
		mockMvc.perform(get(BASE_URI + "/person/1")).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string("{\"href\":\"/api/v1/person/1\",\"name\":\"Piet\"}"));

	}

	@Test
	public void retrieveEntityCollection() throws Exception
	{
		mockMvc.perform(
				get(BASE_URI + "/person").param("start", "5").param("num", "10").param("q[0].operator", "EQUALS")
						.param("q[0].field", "name").param("q[0].value", "Piet"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(
						content()
								.string("{\"href\":\"/api/v1/Person\",\"start\":5,\"num\":10,\"total\":0,\"prevHref\":\"/api/v1/Person?start=0&num=10\",\"items\":[{\"href\":\"/api/v1/person/1\",\"name\":\"Piet\"}]}"));

	}

	@Test
	public void retrieveEntityCollectionPost() throws Exception
	{
		String json = "{start:5, num:10, q:[{operator:EQUALS,field:name,value:Piet}]}";

		mockMvc.perform(post(BASE_URI + "/person").param("_method", "GET").content(json).contentType(APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(
						content()
								.string("{\"href\":\"/api/v1/Person\",\"start\":5,\"num\":10,\"total\":0,\"prevHref\":\"/api/v1/Person?start=0&num=10\",\"items\":[{\"href\":\"/api/v1/person/1\",\"name\":\"Piet\"}]}"));

	}

	@Test
	public void update() throws Exception
	{
		mockMvc.perform(put(BASE_URI + "/person/1").content("{name:Klaas}").contentType(APPLICATION_JSON)).andExpect(
				status().isOk());

		Entity entity = new MapEntity();
		entity.set("name", "Klaas");
		entity.set("id", 1);

		verify(dataService).update(PERSON, entity);
	}

	@Test
	public void updateFromFormPost() throws Exception
	{
		mockMvc.perform(
				post(BASE_URI + "/person/1").param("_method", "PUT").param("name", "Klaas")
						.contentType(APPLICATION_FORM_URLENCODED)).andExpect(status().isNoContent());

		Entity entity = new MapEntity();
		entity.set("name", "Klaas");
		entity.set("id", 1);

		verify(dataService).update(PERSON, entity);
	}

	@Test
	public void updatePost() throws Exception
	{
		mockMvc.perform(
				post(BASE_URI + "/person/1").param("_method", "PUT").content("{name:Klaas}")
						.contentType(APPLICATION_JSON)).andExpect(status().isOk());

		Entity entity = new MapEntity();
		entity.set("name", "Klaas");
		entity.set("id", 1);

		verify(dataService).update(PERSON, entity);
	}

	@Test
	public void unknownEntity() throws Exception
	{
		mockMvc.perform(get(BASE_URI + "/bogus/1")).andExpect(status().isNotFound());
	}

	@Test
	public void molgenisDataAccessException() throws Exception
	{
		when(dataService.findOne(PERSON, 1)).thenThrow(new MolgenisDataAccessException());
		mockMvc.perform(get(BASE_URI + "/person/1")).andExpect(status().isUnauthorized());
	}

	@Configuration
	public static class RestControllerConfig extends WebMvcConfigurerAdapter
	{
		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public RestController restController()
		{
			return new RestController(dataService());
		}
	}

}
