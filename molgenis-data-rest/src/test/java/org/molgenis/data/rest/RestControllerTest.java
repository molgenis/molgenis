package org.molgenis.data.rest;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.mockito.Matchers;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.Updateable;
import org.molgenis.data.meta.WritableMetaDataService;
import org.molgenis.data.rest.RestControllerTest.RestControllerConfig;
import org.molgenis.data.rsql.MolgenisRSQL;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.QueryResolver;
import org.molgenis.messageconverter.CsvHttpMessageConverter;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.token.TokenService;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.ResourceFingerprintRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
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
	private static String ENTITY_NAME = "Person";
	private static Object ENTITY_ID = "p1";
	private static String HREF_ENTITY = BASE_URI + "/" + ENTITY_NAME;
	private static String HREF_ENTITY_META = HREF_ENTITY + "/meta";
	private static String HREF_ENTITY_ID = HREF_ENTITY + "/p1";

	@Autowired
	private RestController restController;

	@Autowired
	private DataService dataService;

	@Autowired
	private WritableMetaDataService metaDataService;

	private MockMvc mockMvc;

	@BeforeMethod
	public void beforeMethod()
	{
		reset(dataService);
		reset(metaDataService);

		Repository repo = mock(Repository.class, withSettings().extraInterfaces(Updateable.class, Queryable.class));

		Entity entityXref = new MapEntity("id");
		entityXref.set("id", ENTITY_ID);
		entityXref.set("name", "PietXREF");

		Entity entity = new MapEntity("id");
		entity.set("id", ENTITY_ID);
		entity.set("name", "Piet");
		entity.set("xrefAttribute", entityXref);

		Entity entity2 = new MapEntity("id");
		entity2.set("id", "p2");
		entity2.set("name", "Klaas");
		entity2.set("xrefAttribute", entityXref);

		List<Entity> entities = new ArrayList<Entity>();
		entities.add(entity2);
		entities.add(entity);

		when(dataService.getEntityNames()).thenReturn(Arrays.asList(ENTITY_NAME));
		when(dataService.getRepositoryByEntityName(ENTITY_NAME)).thenReturn(repo);

		when(dataService.findOne(ENTITY_NAME, ENTITY_ID)).thenReturn(entity);

		Query q = new QueryImpl().eq("name", "Piet").pageSize(10).offset(5);
		when(dataService.findAll(ENTITY_NAME, q)).thenReturn(Arrays.asList(entity));

		Query q2 = new QueryImpl().sort(Sort.Direction.DESC, "name").pageSize(100).offset(0);
		when(dataService.findAll(ENTITY_NAME, q2)).thenReturn(entities);

		DefaultAttributeMetaData attrName = new DefaultAttributeMetaData("name", FieldTypeEnum.STRING);
		attrName.setLookupAttribute(true);

		DefaultAttributeMetaData attrId = new DefaultAttributeMetaData("id", FieldTypeEnum.STRING);
		attrId.setIdAttribute(true);
		attrId.setVisible(false);

		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getAttribute("name")).thenReturn(attrName);
		when(entityMetaData.getIdAttribute()).thenReturn(attrId);
		when(entityMetaData.getAttributes()).thenReturn(Arrays.<AttributeMetaData> asList(attrName, attrId));
		when(entityMetaData.getAtomicAttributes()).thenReturn(Arrays.<AttributeMetaData> asList(attrName, attrId));
		when(entityMetaData.getName()).thenReturn(ENTITY_NAME);
		when(repo.getEntityMetaData()).thenReturn(entityMetaData);
		when(dataService.getEntityMetaData(ENTITY_NAME)).thenReturn(entityMetaData);
		when(repo.getName()).thenReturn(ENTITY_NAME);

		mockMvc = MockMvcBuilders.standaloneSetup(restController)
				.setMessageConverters(new GsonHttpMessageConverter(), new CsvHttpMessageConverter()).build();
	}

	@Test
	public void create() throws Exception
	{
		mockMvc.perform(post(HREF_ENTITY).content("{id:'p1', name:'Piet'}").contentType(APPLICATION_JSON))
				.andExpect(status().isCreated()).andExpect(header().string("Location", HREF_ENTITY_ID));

		verify(dataService).add(Matchers.eq(ENTITY_NAME), Matchers.any(MapEntity.class));
	}

	@Test
	public void createFromFormPost() throws Exception
	{
		mockMvc.perform(
				post(HREF_ENTITY).contentType(APPLICATION_FORM_URLENCODED).param("id", "p1").param("name", "Piet"))
				.andExpect(status().isCreated()).andExpect(header().string("Location", HREF_ENTITY_ID));

		verify(dataService).add(Matchers.eq(ENTITY_NAME), Matchers.any(MapEntity.class));
	}

	@Test
	public void deleteDelete() throws Exception
	{
		mockMvc.perform(delete(HREF_ENTITY_ID)).andExpect(status().isNoContent());
		verify(dataService).delete(ENTITY_NAME, ENTITY_ID);
	}

	@Test
	public void deletePost() throws Exception
	{
		mockMvc.perform(post(HREF_ENTITY_ID).param("_method", "DELETE")).andExpect(status().isNoContent());
		verify(dataService).delete(ENTITY_NAME, ENTITY_ID);
	}

	@Test
	public void deleteAllDelete() throws Exception
	{
		mockMvc.perform(delete(HREF_ENTITY)).andExpect(status().isNoContent());
		verify(dataService).deleteAll(ENTITY_NAME);
	}

	@Test
	public void deleteAllPost() throws Exception
	{
		mockMvc.perform(post(HREF_ENTITY).param("_method", "DELETE")).andExpect(status().isNoContent());
		verify(dataService).deleteAll(ENTITY_NAME);
	}

	@Test
	public void deleteMetaDelete() throws Exception
	{
		mockMvc.perform(delete(HREF_ENTITY_META)).andExpect(status().isNoContent());
		verify(dataService).drop(ENTITY_NAME);
		verify(dataService).removeRepository(ENTITY_NAME);
		verify(metaDataService).removeEntityMetaData(ENTITY_NAME);
	}

	@Test
	public void deleteMetaPost() throws Exception
	{
		mockMvc.perform(post(HREF_ENTITY_META).param("_method", "DELETE")).andExpect(status().isNoContent());
		verify(dataService).drop(ENTITY_NAME);
		verify(dataService).removeRepository(ENTITY_NAME);
		verify(metaDataService).removeEntityMetaData(ENTITY_NAME);
	}

	@Test
	public void retrieveEntityMeta() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_META))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(
						content().string(
								"{\"href\":\"" + HREF_ENTITY_META + "\",\"name\":\"" + ENTITY_NAME
										+ "\",\"attributes\":{\"name\":{\"href\":\"" + HREF_ENTITY_META
										+ "/name\"}},\"idAttribute\":\"id\",\"isAbstract\":false}"));
	}

	@Test
	public void retrieveEntityMetaPost() throws Exception
	{
		String json = "{\"attributes\":[\"name\"]}";
		mockMvc.perform(post(HREF_ENTITY_META).param("_method", "GET").content(json).contentType(APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string("{\"href\":\"" + HREF_ENTITY_META + "\",\"name\":\"" + ENTITY_NAME + "\"}"));
	}

	@Test
	public void retrieveEntityMetaSelectAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_META).param("attributes", "name"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string("{\"href\":\"" + HREF_ENTITY_META + "\",\"name\":\"" + ENTITY_NAME + "\"}"));
	}

	@Test
	public void retrieveEntityMetaExpandAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_META).param("expand", "attributes"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(
						content()
								.string("{\"href\":\""
										+ HREF_ENTITY_META
										+ "\",\"name\":\""
										+ ENTITY_NAME
										+ "\",\"attributes\":{\"name\":{\"href\":\""
										+ HREF_ENTITY_META
										+ "/name\",\"fieldType\":\"STRING\",\"name\":\"name\",\"label\":\"name\",\"attributes\":[],\"nillable\":true,\"readOnly\":false,\"labelAttribute\":false,\"unique\":false,\"lookupAttribute\":true,\"aggregateable\":false}},\"idAttribute\":\"id\",\"isAbstract\":false}"));

	}

	@Test
	public void retrieve() throws Exception
	{
		restController.retrieveEntity(ENTITY_NAME, ENTITY_ID, new String[]
		{}, new String[]
		{});

		mockMvc.perform(get(HREF_ENTITY_ID)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string("{\"href\":\"" + HREF_ENTITY_ID + "\",\"name\":\"Piet\"}"));

	}

	@Test
	public void retrieveSelectAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attributes", "notname")).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string("{\"href\":\"" + HREF_ENTITY_ID + "\"}"));

	}

	@Test
	public void retrieveEntityCollection() throws Exception
	{
		mockMvc.perform(
				get(HREF_ENTITY).param("start", "5").param("num", "10").param("q[0].operator", "EQUALS")
						.param("q[0].field", "name").param("q[0].value", "Piet"))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(
						content().string(
								"{\"href\":\"" + HREF_ENTITY + "\",\"start\":5,\"num\":10,\"total\":0,\"prevHref\":\""
										+ HREF_ENTITY + "?start=0&num=10\",\"items\":[{\"href\":\"" + HREF_ENTITY_ID
										+ "\",\"name\":\"Piet\"}]}"));

	}

	@Test
	public void retrieveEntityCollectionPost() throws Exception
	{
		String json = "{start:5, num:10, q:[{operator:EQUALS,field:name,value:Piet}]}";

		mockMvc.perform(post(HREF_ENTITY).param("_method", "GET").content(json).contentType(APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(
						content().string(
								"{\"href\":\"" + HREF_ENTITY + "\",\"start\":5,\"num\":10,\"total\":0,\"prevHref\":\""
										+ HREF_ENTITY + "?start=0&num=10\",\"items\":[{\"href\":\"" + HREF_ENTITY_ID
										+ "\",\"name\":\"Piet\"}]}"));

	}

	@Test
	public void retrieveEntityAttribute() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID + "/name")).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string("{\"href\":\"" + HREF_ENTITY_ID + "/name\",\"name\":\"Piet\"}"));
	}

	@Test
	public void retrieveEntityAttributePost() throws Exception
	{
		String json = "{\"attributes\":[\"name\"]}";
		mockMvc.perform(
				post(HREF_ENTITY_ID + "/name").param("_method", "GET").content(json).contentType(APPLICATION_JSON))
				.andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string("{\"href\":\"" + HREF_ENTITY_ID + "/name\",\"name\":\"Piet\"}"));
	}

	@Test
	public void retrieveEntityAttributeUnknownAttribute() throws Exception
	{
		Repository repo = mock(Repository.class, withSettings().extraInterfaces(Updateable.class, Queryable.class));

		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getAttribute("name")).thenReturn(null);
		when(repo.getEntityMetaData()).thenReturn(entityMetaData);
		when(dataService.getEntityMetaData(ENTITY_NAME)).thenReturn(entityMetaData);
		when(dataService.getRepositoryByEntityName(ENTITY_NAME)).thenReturn(repo);
		mockMvc.perform(get(HREF_ENTITY_ID + "/name")).andExpect(status().isNotFound());
	}

	@Test
	public void retrieveEntityAttributeUnknownEntity() throws Exception
	{
		when(dataService.findOne(ENTITY_NAME, ENTITY_ID)).thenReturn(null);
		mockMvc.perform(get(HREF_ENTITY_ID + "/name")).andExpect(status().isNotFound());
	}

	@Test
	public void retrieveEntityAttributeXref() throws Exception
	{
		reset(dataService);

		Repository repo = mock(Repository.class, withSettings().extraInterfaces(Updateable.class, Queryable.class));
		when(dataService.getRepositoryByEntityName(ENTITY_NAME)).thenReturn(repo);
		when(dataService.getEntityNames()).thenReturn(Arrays.asList(ENTITY_NAME));
		Entity entityXref = new MapEntity("id");
		entityXref.set("id", ENTITY_ID);
		entityXref.set("xrefValue", "PietXREF");

		Entity entity = new MapEntity("id");
		entity.set("id", ENTITY_ID);
		entity.set("name", entityXref);

		when(dataService.findOne(ENTITY_NAME, ENTITY_ID)).thenReturn(entity);

		DefaultAttributeMetaData attrName = new DefaultAttributeMetaData("name", FieldTypeEnum.XREF);
		EntityMetaData meta = mock(EntityMetaData.class);
		when(dataService.getEntityMetaData(ENTITY_NAME)).thenReturn(meta);
		when(repo.getEntityMetaData()).thenReturn(meta);

		EntityMetaData refMeta = mock(EntityMetaData.class);
		AttributeMetaData attrNameXREF = new DefaultAttributeMetaData("xrefValue", FieldTypeEnum.STRING);
		when(refMeta.getAtomicAttributes()).thenReturn(Arrays.<AttributeMetaData> asList(attrNameXREF));
		attrName.setRefEntity(refMeta);

		DefaultAttributeMetaData attrId = new DefaultAttributeMetaData("id", FieldTypeEnum.INT);
		attrId.setIdAttribute(true);
		attrId.setVisible(false);

		when(meta.getAttribute("name")).thenReturn(attrName);
		when(meta.getIdAttribute()).thenReturn(attrId);
		when(meta.getAttributes()).thenReturn(Arrays.<AttributeMetaData> asList(attrName, attrId));
		when(meta.getAtomicAttributes()).thenReturn(Arrays.<AttributeMetaData> asList(attrName, attrId));
		when(repo.getName()).thenReturn(ENTITY_NAME);
		when(meta.getName()).thenReturn(ENTITY_NAME);

		mockMvc = MockMvcBuilders.standaloneSetup(restController).setMessageConverters(new GsonHttpMessageConverter())
				.build();

		mockMvc.perform(get(HREF_ENTITY_ID + "/name")).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string("{\"href\":\"" + HREF_ENTITY_ID + "/name\",\"xrefValue\":\"PietXREF\"}"));
	}

	@Test
	public void update() throws Exception
	{
		mockMvc.perform(put(HREF_ENTITY_ID).content("{name:Klaas}").contentType(APPLICATION_JSON)).andExpect(
				status().isOk());

		verify(dataService).update(Matchers.eq(ENTITY_NAME), Matchers.any(MapEntity.class));
	}

	@Test
	public void updateInternalRepoNotUpdateable() throws Exception
	{
		Repository repo = mock(Repository.class);
		when(dataService.getRepositoryByEntityName(ENTITY_NAME)).thenReturn(repo);
		doThrow(new MolgenisDataException()).when(dataService).update(anyString(), any(Entity.class));
		mockMvc.perform(put(HREF_ENTITY_ID).content("{name:Klaas}").contentType(APPLICATION_JSON)).andExpect(
				status().isBadRequest());
	}

	@Test
	public void updateInternalRepoIdAttributeIsNull() throws Exception
	{
		Repository repo = mock(Repository.class, withSettings().extraInterfaces(Updateable.class, Queryable.class));
		when(dataService.getRepositoryByEntityName(ENTITY_NAME)).thenReturn(repo);
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(entityMetaData.getIdAttribute()).thenReturn(null);
		when(repo.getEntityMetaData()).thenReturn(entityMetaData);
		when(dataService.getEntityMetaData(ENTITY_NAME)).thenReturn(entityMetaData);
		mockMvc.perform(put(HREF_ENTITY_ID).content("{name:Klaas}").contentType(APPLICATION_JSON)).andExpect(
				status().isInternalServerError());
	}

	@Test
	public void updateInternalRepoExistingIsNull() throws Exception
	{
		when(dataService.findOne(ENTITY_NAME, ENTITY_ID)).thenReturn(null);

		mockMvc.perform(put(HREF_ENTITY_ID).content("{name:Klaas}").contentType(APPLICATION_JSON)).andExpect(
				status().isNotFound());
	}

	@Test
	public void updateAttribute() throws Exception
	{
		mockMvc.perform(
				post(HREF_ENTITY_ID + "/name").param("_method", "PUT").content("Klaas").contentType(APPLICATION_JSON))
				.andExpect(status().isOk());
		verify(dataService).update(Matchers.eq(ENTITY_NAME), Matchers.any(MapEntity.class));
	}

	@Test
	public void updateAttribute_unknownEntity() throws Exception
	{
		mockMvc.perform(
				post(BASE_URI + "/unknownentity/" + ENTITY_ID + "/name").param("_method", "PUT").content("Klaas")
						.contentType(APPLICATION_JSON)).andExpect(status().isNotFound());
	}

	@Test
	public void updateAttribute_unknownEntityId() throws Exception
	{
		mockMvc.perform(
				post(HREF_ENTITY + "/666" + "/name").param("_method", "PUT").content("Klaas")
						.contentType(APPLICATION_JSON)).andExpect(status().isNotFound());
	}

	@Test
	public void updateAttribute_unknownAttribute() throws Exception
	{
		mockMvc.perform(
				post(HREF_ENTITY_ID + "/unknownattribute").param("_method", "PUT").content("Klaas")
						.contentType(APPLICATION_JSON)).andExpect(status().isNotFound());
	}

	@Test
	public void updateFromFormPost() throws Exception
	{
		mockMvc.perform(
				post(HREF_ENTITY_ID).param("_method", "PUT").param("name", "Klaas")
						.contentType(APPLICATION_FORM_URLENCODED)).andExpect(status().isNoContent());

		verify(dataService).update(Matchers.eq(ENTITY_NAME), Matchers.any(MapEntity.class));
	}

	@Test
	public void updatePost() throws Exception
	{
		mockMvc.perform(
				post(HREF_ENTITY_ID).param("_method", "PUT").content("{name:Klaas}").contentType(APPLICATION_JSON))
				.andExpect(status().isOk());

		verify(dataService).update(Matchers.eq(ENTITY_NAME), Matchers.any(MapEntity.class));
	}

	@Test
	public void handleUnknownEntityException() throws Exception
	{
		mockMvc.perform(get(BASE_URI + "/bogus/1")).andExpect(status().isNotFound());
	}

	@Test
	public void molgenisDataAccessException() throws Exception
	{
		when(dataService.findOne(ENTITY_NAME, ENTITY_ID)).thenThrow(new MolgenisDataAccessException());
		mockMvc.perform(get(HREF_ENTITY_ID)).andExpect(status().isUnauthorized());
	}

	@Test
	public void retrieveEntityCollectionCsv() throws Exception
	{
		mockMvc.perform(get(BASE_URI + "/csv/Person").param("start", "5").param("num", "10").param("q", "name==Piet"))
				.andExpect(status().isOk()).andExpect(content().contentType("text/csv"))
				.andExpect(content().string("\"name\",\"id\"\n\"Piet\",\"p1\"\n"));
	}

	@Test
	public void retrieveSortedEntityCollectionCsv() throws Exception
	{
		mockMvc.perform(get(BASE_URI + "/csv/Person").param("sortColumn", "name").param("sortOrder", "DESC"))
				.andExpect(status().isOk()).andExpect(content().contentType("text/csv"))
				.andExpect(content().string("\"name\",\"id\"\n\"Klaas\",\"p2\"\n\"Piet\",\"p1\"\n"));
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
		public WritableMetaDataService metaDataService()
		{
			return mock(WritableMetaDataService.class);
		}

		@Bean
		public TokenService tokenService()
		{
			return mock(TokenService.class);
		}

		@Bean
		public AuthenticationManager authenticationManager()
		{
			return mock(AuthenticationManager.class);
		}

		@Bean
		public QueryResolver queryResolver()
		{
			return new QueryResolver(dataService());
		}

		@Bean
		public MolgenisPermissionService molgenisPermissionService()
		{
			return mock(MolgenisPermissionService.class);
		}

		@Bean
		public RestController restController()
		{
			return new RestController(dataService(), metaDataService(), tokenService(), authenticationManager(),
					molgenisPermissionService(), new MolgenisRSQL(), new ResourceFingerprintRegistry());
		}
	}

}
