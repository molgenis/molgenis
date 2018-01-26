package org.molgenis.data.rest;

import cz.jirutka.rsql.parser.RSQLParser;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.molgenis.core.ui.data.rsql.MolgenisRSQL;
import org.molgenis.core.ui.messageconverter.CsvHttpMessageConverter;
import org.molgenis.core.ui.util.GsonConfig;
import org.molgenis.core.util.GsonHttpMessageConverter;
import org.molgenis.data.*;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.rest.RestControllerTest.RestControllerConfig;
import org.molgenis.data.rest.service.RestService;
import org.molgenis.data.rest.service.ServletUriComponentsBuilderFactory;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.token.TokenExtractor;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.web.exception.FallbackExceptionHandler;
import org.molgenis.web.exception.GlobalControllerExceptionHandler;
import org.molgenis.web.exception.SpringExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Locale.ENGLISH;
import static org.mockito.ArgumentMatchers.any;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;

@WebAppConfiguration
@ContextConfiguration(classes = { RestControllerConfig.class, GsonConfig.class })
public class RestControllerTest extends AbstractTestNGSpringContextTests
{
	private static String ENTITY_NAME = "Person";
	private static String ENTITY_UNTYPED_ID = "p1";
	private static String HREF_ENTITY = RestController.BASE_URI + '/' + ENTITY_NAME;
	private static String HREF_ENTITY_META = HREF_ENTITY + "/meta";
	private static String HREF_ENTITY_ID = HREF_ENTITY + "/p1";

	private static final String CSV_HEADER = "\"name\",\"id\",\"enum\",\"int\"\n";
	private static final String ENTITY_COLLECTION_RESPONSE_STRING = "{\"href\":\"" + HREF_ENTITY
			+ "\",\"meta\":{\"href\":\"/api/v1/Person/meta\",\"hrefCollection\":\"/api/v1/Person\",\"name\":\"Person\",\"attributes\":{\"name\":{\"href\":\"/api/v1/Person/meta/name\"},\"id\":{\"href\":\"/api/v1/Person/meta/id\"},\"enum\":{\"href\":\"/api/v1/Person/meta/enum\"},\"int\":{\"href\":\"/api/v1/Person/meta/int\"}},\"idAttribute\":\"id\",\"isAbstract\":false,\"languageCode\":\"en\",\"writable\":false},\"start\":5,\"num\":10,\"total\":0,\"prevHref\":\""
			+ HREF_ENTITY + "?start=0&num=10\",\"items\":[{\"href\":\"" + HREF_ENTITY_ID
			+ "\",\"name\":\"Piet\",\"id\":\"p1\",\"enum\":\"enum1\",\"int\":1}]}";
	private static final String ENTITY_META_RESPONSE_STRING =
			"{\"href\":\"" + HREF_ENTITY_META + "\",\"hrefCollection\":\"/api/v1/Person\",\"name\":\"" + ENTITY_NAME
					+ "\",\"attributes\":{\"name\":{\"href\":\"" + HREF_ENTITY_META
					+ "/name\"},\"id\":{\"href\":\"/api/v1/Person/meta/id\"},\"enum\":{\"href\":\"/api/v1/Person/meta/enum\"},\"int\":{\"href\":\"/api/v1/Person/meta/int\"}},\"idAttribute\":\"id\",\"isAbstract\":false,\"languageCode\":\"en\",\"writable\":false}";

	@Autowired
	private RestController restController;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private DataService dataService;

	@Autowired
	private MetaDataService metaDataService;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private TokenService tokenService;

	private MockMvc mockMvc;

	@Autowired
	private MessageSource messageSource;

	@Mock
	private LocaleResolver localeResolver;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		Mockito.reset(permissionService, dataService, metaDataService, tokenService);

		MessageSourceHolder.setMessageSource(messageSource);
		Mockito.when(dataService.getMeta()).thenReturn(metaDataService);

		@SuppressWarnings("unchecked")
		Repository<Entity> repo = Mockito.mock(Repository.class);

		// test entity meta data
		EntityType entityType = Mockito.mock(EntityType.class);

		Attribute attrId = Mockito.when(Mockito.mock(Attribute.class).getName()).thenReturn("id").getMock();
		Mockito.when(attrId.getLabel()).thenReturn("id");
		Mockito.when(attrId.getLabel("en")).thenReturn("id");
		Mockito.when(attrId.getDataType()).thenReturn(AttributeType.STRING);
		Mockito.when(attrId.isReadOnly()).thenReturn(true);
		Mockito.when(attrId.isUnique()).thenReturn(true);
		Mockito.when(attrId.isNillable()).thenReturn(false);
		Mockito.when(attrId.isVisible()).thenReturn(false);
		Mockito.when(attrId.getChildren()).thenReturn(emptyList());
		Mockito.when(attrId.getEnumOptions()).thenReturn(emptyList());

		Attribute attrName = Mockito.when(Mockito.mock(Attribute.class).getName()).thenReturn("name").getMock();
		Mockito.when(attrName.getLabel()).thenReturn("name");
		Mockito.when(attrName.getLabel("en")).thenReturn("name");
		Mockito.when(attrName.getDataType()).thenReturn(AttributeType.STRING);
		Mockito.when(attrName.isNillable()).thenReturn(true);
		Mockito.when(attrName.isVisible()).thenReturn(true);
		Mockito.when(attrName.getChildren()).thenReturn(emptyList());
		Mockito.when(attrName.getEnumOptions()).thenReturn(emptyList());

		Attribute attrEnum = Mockito.when(Mockito.mock(Attribute.class).getName()).thenReturn("enum").getMock();
		Mockito.when(attrEnum.getLabel()).thenReturn("enum");
		Mockito.when(attrEnum.getLabel("en")).thenReturn("enum");
		Mockito.when(attrEnum.getDataType()).thenReturn(AttributeType.ENUM);
		Mockito.when(attrEnum.getEnumOptions()).thenReturn(singletonList("enum0, enum1"));
		Mockito.when(attrEnum.isNillable()).thenReturn(true);
		Mockito.when(attrEnum.isVisible()).thenReturn(true);
		Mockito.when(attrEnum.getChildren()).thenReturn(emptyList());

		Attribute attrInt = Mockito.when(Mockito.mock(Attribute.class).getName()).thenReturn("int").getMock();
		Mockito.when(attrInt.getLabel()).thenReturn("int");
		Mockito.when(attrInt.getLabel("en")).thenReturn("int");
		Mockito.when(attrInt.getDataType()).thenReturn(AttributeType.INT);
		Mockito.when(attrInt.isNillable()).thenReturn(true);
		Mockito.when(attrInt.isVisible()).thenReturn(true);
		Mockito.when(attrInt.getChildren()).thenReturn(emptyList());

		Mockito.when(entityType.getAttribute("id")).thenReturn(attrId);
		Mockito.when(entityType.getAttribute("name")).thenReturn(attrName);
		Mockito.when(entityType.getAttribute("enum")).thenReturn(attrEnum);
		Mockito.when(entityType.getAttribute("int")).thenReturn(attrInt);
		Mockito.when(entityType.getMappedByAttributes()).thenReturn(Stream.empty());
		Mockito.when(entityType.getIdAttribute()).thenReturn(attrId);
		//TODO: This upgrades the test to mockito 2 but actually shows an error in the test
		Mockito.when(entityType.getLookupAttributes()).thenReturn(null);
		Mockito.when(entityType.getAttributes()).thenReturn(asList(attrName, attrId, attrEnum, attrInt));
		Mockito.when(entityType.getAtomicAttributes()).thenReturn(asList(attrName, attrId, attrEnum, attrInt));
		Mockito.when(entityType.getId()).thenReturn(ENTITY_NAME);
		Mockito.when(entityType.getLabel("en")).thenReturn(null);

		Mockito.when(repo.getEntityType()).thenReturn(entityType);
		Mockito.when(repo.getName()).thenReturn(ENTITY_NAME);
		Mockito.when(dataService.getEntityType(ENTITY_NAME)).thenReturn(entityType);
		Mockito.when(entityManager.create(entityType, POPULATE)).thenReturn(new DynamicEntity(entityType));

		// test entities
		Entity entityXref = new DynamicEntity(entityType);
		entityXref.set("id", ENTITY_UNTYPED_ID);
		entityXref.set("name", "PietXREF");

		Entity entity = new DynamicEntity(entityType);
		entity.set("id", ENTITY_UNTYPED_ID);
		entity.set("name", "Piet");
		entity.set("enum", "enum1");
		entity.set("int", 1);

		Entity entity2 = new DynamicEntity(entityType);
		entity2.set("id", "p2");
		entity2.set("name", "Klaas");
		entity2.set("int", 2);

		Mockito.when(dataService.getEntityTypeIds()).thenReturn(Stream.of(ENTITY_NAME));
		Mockito.when(dataService.getRepository(ENTITY_NAME)).thenReturn(repo);

		Mockito.when(dataService.findOneById(ArgumentMatchers.eq(ENTITY_NAME), ArgumentMatchers.eq(ENTITY_UNTYPED_ID),
				ArgumentMatchers.any(Fetch.class))).thenReturn(entity);
		Mockito.when(dataService.findOneById(ENTITY_NAME, ENTITY_UNTYPED_ID)).thenReturn(entity);

		Query<Entity> q = new QueryImpl<>().eq("name", "Piet").pageSize(10).offset(5);
		Mockito.when(dataService.findAll(ENTITY_NAME, q)).thenReturn(Stream.of(entity));

		Query<Entity> q2 = new QueryImpl<>().sort(new Sort().on("name", Sort.Direction.DESC)).pageSize(100).offset(0);
		Mockito.when(dataService.findAll(ENTITY_NAME, q2)).thenReturn(Stream.of(entity2, entity));

		Mockito.when(localeResolver.resolveLocale(any())).thenReturn(ENGLISH);
		mockMvc = MockMvcBuilders.standaloneSetup(restController)
								 .setMessageConverters(gsonHttpMessageConverter, new CsvHttpMessageConverter())
								 .setCustomArgumentResolvers(new TokenExtractor())
								 .setControllerAdvice(new GlobalControllerExceptionHandler(),
										 new FallbackExceptionHandler(), new SpringExceptionHandler())
								 .setLocaleResolver(localeResolver)
								 .build();
	}

	@Test
	public void loginPasswordReset() throws Exception
	{
		String username = "henk";
		String password = "123henk";

		Authentication authentication = Mockito.mock(Authentication.class);
		Mockito.when(authentication.isAuthenticated()).thenReturn(true);
		Mockito.when(authentication.getName()).thenReturn(username);
		Mockito.when(
				authenticationManager.authenticate(ArgumentMatchers.any(UsernamePasswordAuthenticationToken.class)))
			   .thenReturn(authentication);

		User user = Mockito.mock(User.class);
		Mockito.when(user.isChangePassword()).thenReturn(true);
		Mockito.when(dataService.findOne(UserMetaData.USER, new QueryImpl<User>().eq(UserMetaData.USERNAME, username),
				User.class)).thenReturn(user);

		mockMvc.perform(MockMvcRequestBuilders.post(RestController.BASE_URI + "/login")
											  .content(String.format("{username: '%s', password: '%s'}", username,
													  password))
											  .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	@Test
	public void testLogoutTokenInHeader() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post(RestController.BASE_URI + "/logout")
											  .header(TokenExtractor.TOKEN_HEADER, "abcde")
											  .content("")).andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(tokenService).removeToken("abcde");
	}

	@Test
	public void testLogoutTokenInParam() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post(RestController.BASE_URI + "/logout")
											  .param(TokenExtractor.TOKEN_PARAMETER, "abcde")
											  .content("")).andExpect(MockMvcResultMatchers.status().isOk());
		Mockito.verify(tokenService).removeToken("abcde");
	}

	@Test
	public void testLogoutNoToken() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post(RestController.BASE_URI + "/logout").content(""))
			   .andExpect(MockMvcResultMatchers.status().isBadRequest());
		Mockito.verifyZeroInteractions(tokenService);
	}

	@Test
	public void create() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post(HREF_ENTITY)
											  .content("{id:'p1', name:'Piet'}")
											  .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.status().isCreated())
			   .andExpect(MockMvcResultMatchers.header().string("Location", HREF_ENTITY_ID));

		Mockito.verify(dataService).add(ArgumentMatchers.eq(ENTITY_NAME), ArgumentMatchers.any(Entity.class));
	}

	@Test
	public void createFromFormPost() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post(HREF_ENTITY)
											  .contentType(MediaType.APPLICATION_FORM_URLENCODED)
											  .param("id", "p1")
											  .param("name", "Piet"))
			   .andExpect(MockMvcResultMatchers.status().isCreated())
			   .andExpect(MockMvcResultMatchers.header().string("Location", HREF_ENTITY_ID));

		Mockito.verify(dataService).add(ArgumentMatchers.eq(ENTITY_NAME), ArgumentMatchers.any(Entity.class));
	}

	@Test
	public void deleteDelete() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.delete(HREF_ENTITY_ID))
			   .andExpect(MockMvcResultMatchers.status().isNoContent());
		Mockito.verify(dataService).deleteById(ENTITY_NAME, ENTITY_UNTYPED_ID);
	}

	@Test
	public void deletePost() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post(HREF_ENTITY_ID).param("_method", "DELETE"))
			   .andExpect(MockMvcResultMatchers.status().isNoContent());
		Mockito.verify(dataService).deleteById(ENTITY_NAME, ENTITY_UNTYPED_ID);
	}

	@Test
	public void deleteAllDelete() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.delete(HREF_ENTITY))
			   .andExpect(MockMvcResultMatchers.status().isNoContent());
		Mockito.verify(dataService).deleteAll(ENTITY_NAME);
	}

	@Test
	public void deleteAllPost() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post(HREF_ENTITY).param("_method", "DELETE"))
			   .andExpect(MockMvcResultMatchers.status().isNoContent());
		Mockito.verify(dataService).deleteAll(ENTITY_NAME);
	}

	@Test
	public void deleteMetaDelete() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.delete(HREF_ENTITY_META))
			   .andExpect(MockMvcResultMatchers.status().isNoContent());
		Mockito.verify(metaDataService).deleteEntityType(ENTITY_NAME);
	}

	@Test
	public void deleteMetaPost() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post(HREF_ENTITY_META).param("_method", "DELETE"))
			   .andExpect(MockMvcResultMatchers.status().isNoContent());
		Mockito.verify(metaDataService).deleteEntityType(ENTITY_NAME);
	}

	@Test
	public void retrieveEntityType() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.get(HREF_ENTITY_META))
			   .andExpect(MockMvcResultMatchers.status().isOk())
			   .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.content().string(ENTITY_META_RESPONSE_STRING));
	}

	@Test
	public void retrieveEntityTypeWritable() throws Exception
	{
		Mockito.when(permissionService.hasPermissionOnEntityType(ENTITY_NAME, Permission.WRITE)).thenReturn(true);
		Mockito.when(dataService.getCapabilities(ENTITY_NAME))
			   .thenReturn(new HashSet<>(singletonList(RepositoryCapability.WRITABLE)));
		mockMvc.perform(MockMvcRequestBuilders.get(HREF_ENTITY_META))
			   .andExpect(MockMvcResultMatchers.status().isOk())
			   .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.content()
											   .string("{\"href\":\"" + HREF_ENTITY_META
													   + "\",\"hrefCollection\":\"/api/v1/Person\",\"name\":\""
													   + ENTITY_NAME + "\",\"attributes\":{\"name\":{\"href\":\""
													   + HREF_ENTITY_META
													   + "/name\"},\"id\":{\"href\":\"/api/v1/Person/meta/id\"},\"enum\":{\"href\":\"/api/v1/Person/meta/enum\"},\"int\":{\"href\":\"/api/v1/Person/meta/int\"}},\"idAttribute\":\"id\",\"isAbstract\":false,\"languageCode\":\"en\",\"writable\":true}"));
	}

	@Test
	public void retrieveEntityTypeNotWritable() throws Exception
	{
		Mockito.when(permissionService.hasPermissionOnEntityType(ENTITY_NAME, Permission.WRITE)).thenReturn(true);
		Mockito.when(dataService.getCapabilities(ENTITY_NAME))
			   .thenReturn(new HashSet<>(singletonList(RepositoryCapability.QUERYABLE)));
		mockMvc.perform(MockMvcRequestBuilders.get(HREF_ENTITY_META))
			   .andExpect(MockMvcResultMatchers.status().isOk())
			   .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.content().string(ENTITY_META_RESPONSE_STRING));
	}

	@Test
	public void retrieveEntityTypePost() throws Exception
	{
		String json = "{\"attributes\":[\"name\"]}";
		mockMvc.perform(MockMvcRequestBuilders.post(HREF_ENTITY_META)
											  .param("_method", "GET")
											  .content(json)
											  .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.status().isOk())
			   .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.content()
											   .string("{\"href\":\"" + HREF_ENTITY_META
													   + "\",\"hrefCollection\":\"/api/v1/Person\",\"name\":\""
													   + ENTITY_NAME
													   + "\",\"languageCode\":\"en\",\"writable\":false}"));
	}

	@Test
	public void retrieveEntityTypeSelectAttributes() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.get(HREF_ENTITY_META).param("attributes", "name"))
			   .andExpect(MockMvcResultMatchers.status().isOk())
			   .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.content()
											   .string("{\"href\":\"" + HREF_ENTITY_META
													   + "\",\"hrefCollection\":\"/api/v1/Person\",\"name\":\""
													   + ENTITY_NAME
													   + "\",\"languageCode\":\"en\",\"writable\":false}"));
	}

	@Test
	public void retrieveEntityTypeExpandAttributes() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.get(HREF_ENTITY_META).param("expand", "attributes"))
			   .andExpect(MockMvcResultMatchers.status().isOk())
			   .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.content()
											   .string("{\"href\":\"/api/v1/Person/meta\",\"hrefCollection\":\"/api/v1/Person\",\"name\":\"Person\",\"attributes\":{\"name\":{\"href\":\"/api/v1/Person/meta/name\",\"fieldType\":\"STRING\",\"name\":\"name\",\"label\":\"name\",\"attributes\":[],\"enumOptions\":[],\"maxLength\":255,\"auto\":false,\"nillable\":true,\"readOnly\":false,\"labelAttribute\":false,\"unique\":false,\"visible\":true,\"lookupAttribute\":false,\"isAggregatable\":false},\"id\":{\"href\":\"/api/v1/Person/meta/id\",\"fieldType\":\"STRING\",\"name\":\"id\",\"label\":\"id\",\"attributes\":[],\"enumOptions\":[],\"maxLength\":255,\"auto\":false,\"nillable\":false,\"readOnly\":true,\"labelAttribute\":false,\"unique\":true,\"visible\":false,\"lookupAttribute\":false,\"isAggregatable\":false},\"enum\":{\"href\":\"/api/v1/Person/meta/enum\",\"fieldType\":\"ENUM\",\"name\":\"enum\",\"label\":\"enum\",\"attributes\":[],\"enumOptions\":[\"enum0, enum1\"],\"maxLength\":255,\"auto\":false,\"nillable\":true,\"readOnly\":false,\"labelAttribute\":false,\"unique\":false,\"visible\":true,\"lookupAttribute\":false,\"isAggregatable\":false},\"int\":{\"href\":\"/api/v1/Person/meta/int\",\"fieldType\":\"INT\",\"name\":\"int\",\"label\":\"int\",\"attributes\":[],\"enumOptions\":[],\"auto\":false,\"nillable\":true,\"readOnly\":false,\"labelAttribute\":false,\"unique\":false,\"visible\":true,\"lookupAttribute\":false,\"isAggregatable\":false}},\"idAttribute\":\"id\",\"isAbstract\":false,\"languageCode\":\"en\",\"writable\":false}"));
	}

	@Test
	public void retrieve() throws Exception
	{
		restController.retrieveEntity(ENTITY_NAME, ENTITY_UNTYPED_ID, new String[] {}, new String[] {});

		mockMvc.perform(MockMvcRequestBuilders.get(HREF_ENTITY_ID))
			   .andExpect(MockMvcResultMatchers.status().isOk())
			   .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.content()
											   .string("{\"href\":\"" + HREF_ENTITY_ID
													   + "\",\"name\":\"Piet\",\"id\":\"p1\",\"enum\":\"enum1\",\"int\":1}"));

	}

	@Test
	public void retrieveSelectAttributes() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.get(HREF_ENTITY_ID).param("attributes", "notname"))
			   .andExpect(MockMvcResultMatchers.status().isOk())
			   .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.content().string("{\"href\":\"" + HREF_ENTITY_ID + "\"}"));

	}

	@Test
	public void retrieveEntityCollection() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.get(HREF_ENTITY)
											  .param("start", "5")
											  .param("num", "10")
											  .param("q[0].operator", "EQUALS")
											  .param("q[0].field", "name")
											  .param("q[0].value", "Piet"))
			   .andExpect(MockMvcResultMatchers.status().isOk())
			   .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.content().string(ENTITY_COLLECTION_RESPONSE_STRING));

	}

	@Test
	public void retrieveEntityCollectionPost() throws Exception
	{
		String json = "{start:5, num:10, q:[{operator:EQUALS,field:name,value:Piet}]}";

		mockMvc.perform(MockMvcRequestBuilders.post(HREF_ENTITY)
											  .param("_method", "GET")
											  .content(json)
											  .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.status().isOk())
			   .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.content().string(ENTITY_COLLECTION_RESPONSE_STRING));
	}

	@Test
	public void retrieveEntityAttribute() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.get(HREF_ENTITY_ID + "/name"))
			   .andExpect(MockMvcResultMatchers.status().isOk())
			   .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.content()
											   .string("{\"href\":\"" + HREF_ENTITY_ID + "/name\",\"name\":\"Piet\"}"));
	}

	@Test
	public void retrieveAttributeUnknownEntity() throws Exception
	{
		String HREF_UNKNOWN_ENTITY_META = RestController.BASE_URI + "/unknown/meta";
		mockMvc.perform(MockMvcRequestBuilders.get(HREF_UNKNOWN_ENTITY_META + "/attribute"))
			   .andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	public void retrieveEntityAttributePost() throws Exception
	{
		String json = "{\"attributes\":[\"name\"]}";
		mockMvc.perform(MockMvcRequestBuilders.post(HREF_ENTITY_ID + "/name")
											  .param("_method", "GET")
											  .content(json)
											  .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.status().isOk())
			   .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.content()
											   .string("{\"href\":\"" + HREF_ENTITY_ID + "/name\",\"name\":\"Piet\"}"));
	}

	@Test
	public void retrieveEntityAttributeUnknownAttribute() throws Exception
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> repo = Mockito.mock(Repository.class);

		EntityType entityType = Mockito.mock(EntityType.class);
		Mockito.when(entityType.getAttribute("name")).thenReturn(null);
		Attribute idAttr = Mockito.when(Mockito.mock(Attribute.class).getDataType())
								  .thenReturn(AttributeType.STRING)
								  .getMock();
		Mockito.when(entityType.getIdAttribute()).thenReturn(idAttr);
		Mockito.when(repo.getEntityType()).thenReturn(entityType);
		Mockito.when(dataService.getEntityType(ENTITY_NAME)).thenReturn(entityType);
		Mockito.when(dataService.getRepository(ENTITY_NAME)).thenReturn(repo);
		mockMvc.perform(MockMvcRequestBuilders.get(HREF_ENTITY_ID + "/name"))
			   .andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	public void retrieveEntityAttributeUnknownEntity() throws Exception
	{
		Mockito.when(dataService.findOneById(ENTITY_NAME, ENTITY_UNTYPED_ID)).thenReturn(null);
		mockMvc.perform(MockMvcRequestBuilders.get(HREF_ENTITY_ID + "/name"))
			   .andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	public void retrieveEntityAttributeXref() throws Exception
	{
		Mockito.reset(dataService);

		@SuppressWarnings("unchecked")
		Repository<Entity> repo = Mockito.mock(Repository.class);
		Mockito.when(dataService.getRepository(ENTITY_NAME)).thenReturn(repo);
		Mockito.when(dataService.getEntityTypeIds()).thenReturn(Stream.of(ENTITY_NAME));

		// entity meta data
		EntityType refEntityType = Mockito.when(Mockito.mock(EntityType.class).getId())
										  .thenReturn("refEntity")
										  .getMock();

		Attribute attrId = Mockito.when(Mockito.mock(Attribute.class).getName()).thenReturn("id").getMock();
		Mockito.when(attrId.getLabel()).thenReturn("id");
		Mockito.when(attrId.getLabel(ArgumentMatchers.anyString())).thenReturn("id");
		Mockito.when(attrId.getDataType()).thenReturn(AttributeType.STRING);
		Mockito.when(attrId.isReadOnly()).thenReturn(true);
		Mockito.when(attrId.isUnique()).thenReturn(true);
		Mockito.when(attrId.isNillable()).thenReturn(false);
		Mockito.when(attrId.isVisible()).thenReturn(false);
		Mockito.when(attrId.getChildren()).thenReturn(emptyList());
		Mockito.when(attrId.getEnumOptions()).thenReturn(emptyList());

		Attribute attrName = Mockito.when(Mockito.mock(Attribute.class).getName()).thenReturn("name").getMock();
		Mockito.when(attrName.getLabel()).thenReturn("name");
		Mockito.when(attrName.getLabel(ArgumentMatchers.anyString())).thenReturn("name");
		Mockito.when(attrName.getDataType()).thenReturn(AttributeType.STRING);
		Mockito.when(attrName.isNillable()).thenReturn(true);
		Mockito.when(attrName.isVisible()).thenReturn(true);
		Mockito.when(attrName.getChildren()).thenReturn(emptyList());
		Mockito.when(attrName.getEnumOptions()).thenReturn(emptyList());

		Mockito.when(refEntityType.getAttribute("id")).thenReturn(attrId);
		Mockito.when(refEntityType.getAttribute("name")).thenReturn(attrName);
		Mockito.when(refEntityType.getIdAttribute()).thenReturn(attrId);
		Mockito.when(refEntityType.getAttributes()).thenReturn(asList(attrId, attrName));
		Mockito.when(refEntityType.getAtomicAttributes()).thenReturn(asList(attrId, attrName));
		Mockito.when(refEntityType.getId()).thenReturn("refEntity");

		EntityType entityType = Mockito.when(Mockito.mock(EntityType.class).getId()).thenReturn(ENTITY_NAME).getMock();

		Attribute attrXref = Mockito.when(Mockito.mock(Attribute.class).getName()).thenReturn("xrefValue").getMock();
		Mockito.when(attrXref.getLabel()).thenReturn("xrefValue");
		Mockito.when(attrXref.getLabel(ArgumentMatchers.anyString())).thenReturn("xrefValue");
		Mockito.when(attrXref.getDataType()).thenReturn(AttributeType.XREF);
		Mockito.when(attrXref.isNillable()).thenReturn(true);
		Mockito.when(attrXref.isVisible()).thenReturn(true);
		Mockito.when(attrXref.getChildren()).thenReturn(emptyList());
		Mockito.when(attrXref.getEnumOptions()).thenReturn(emptyList());
		Mockito.when(attrXref.getRefEntity()).thenReturn(refEntityType);

		Mockito.when(entityType.getAttribute("id")).thenReturn(attrId);
		Mockito.when(entityType.getAttribute("xrefValue")).thenReturn(attrXref);
		Mockito.when(entityType.getIdAttribute()).thenReturn(attrId);
		Mockito.when(entityType.getAttributes()).thenReturn(asList(attrId, attrXref));
		Mockito.when(entityType.getAtomicAttributes()).thenReturn(asList(attrId, attrXref));
		Mockito.when(entityType.getId()).thenReturn(ENTITY_NAME);

		Entity entityXref = new DynamicEntity(refEntityType);
		entityXref.set("id", ENTITY_UNTYPED_ID);
		entityXref.set("name", "Piet");

		Entity entity = new DynamicEntity(entityType);
		entity.set("id", ENTITY_UNTYPED_ID);
		entity.set("xrefValue", entityXref);

		Mockito.when(dataService.findOneById(ENTITY_NAME, ENTITY_UNTYPED_ID)).thenReturn(entity);
		Mockito.when(dataService.findOneById("refEntity", ENTITY_UNTYPED_ID)).thenReturn(entityXref);
		Mockito.when(dataService.getEntityType(ENTITY_NAME)).thenReturn(entityType);
		Mockito.when(dataService.getEntityType("refEntity")).thenReturn(refEntityType);
		mockMvc = MockMvcBuilders.standaloneSetup(restController)
								 .setMessageConverters(gsonHttpMessageConverter)
								 .build();

		mockMvc.perform(MockMvcRequestBuilders.get(HREF_ENTITY_ID + "/xrefValue"))
			   .andExpect(MockMvcResultMatchers.status().isOk())
			   .andExpect(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.content()
											   .string("{\"href\":\"/api/v1/Person/p1/xrefValue\",\"id\":\"p1\",\"name\":\"Piet\"}"));
	}

	@Test
	public void update() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.put(HREF_ENTITY_ID)
											  .content("{name:Klaas}")
											  .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(dataService).update(ArgumentMatchers.eq(ENTITY_NAME), ArgumentMatchers.any(Entity.class));
	}

	@Test
	public void updateInternalRepoNotUpdateable() throws Exception
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> repo = Mockito.mock(Repository.class);
		Mockito.when(dataService.getRepository(ENTITY_NAME)).thenReturn(repo);
		Mockito.doThrow(new MolgenisDataException())
			   .when(dataService)
			   .update(ArgumentMatchers.anyString(), ArgumentMatchers.any(Entity.class));
		mockMvc.perform(MockMvcRequestBuilders.put(HREF_ENTITY_ID)
											  .content("{name:Klaas}")
											  .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.status().isBadRequest());
	}

	@Test
	public void updateInternalRepoIdAttributeIsNull() throws Exception
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> repo = Mockito.mock(Repository.class);
		Mockito.when(dataService.getRepository(ENTITY_NAME)).thenReturn(repo);
		EntityType entityType = Mockito.mock(EntityType.class);
		Mockito.when(entityType.getIdAttribute()).thenReturn(null);
		Mockito.when(repo.getEntityType()).thenReturn(entityType);
		Mockito.when(dataService.getEntityType(ENTITY_NAME)).thenReturn(entityType);
		mockMvc.perform(MockMvcRequestBuilders.put(HREF_ENTITY_ID)
											  .content("{name:Klaas}")
											  .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.status().isInternalServerError());
	}

	@Test
	public void updateInternalRepoExistingIsNull() throws Exception
	{
		Mockito.when(dataService.findOneById(ArgumentMatchers.eq(ENTITY_NAME), ArgumentMatchers.eq(ENTITY_UNTYPED_ID),
				ArgumentMatchers.any(Fetch.class))).thenReturn(null);

		mockMvc.perform(MockMvcRequestBuilders.put(HREF_ENTITY_ID)
											  .content("{name:Klaas}")
											  .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	public void updateAttribute() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post(HREF_ENTITY_ID + "/name")
											  .param("_method", "PUT")
											  .content("Klaas")
											  .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.status().isOk());
		Mockito.verify(dataService).update(ArgumentMatchers.eq(ENTITY_NAME), ArgumentMatchers.any(Entity.class));
	}

	@Test
	public void updateAttribute_unknownEntity() throws Exception
	{
		mockMvc.perform(
				MockMvcRequestBuilders.post(RestController.BASE_URI + "/unknownentity/" + ENTITY_UNTYPED_ID + "/name")
									  .param("_method", "PUT")
									  .content("Klaas")
									  .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	public void updateAttribute_unknownEntityId() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post(HREF_ENTITY + "/666" + "/name")
											  .param("_method", "PUT")
											  .content("Klaas")
											  .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	public void updateAttribute_unknownAttribute() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post(HREF_ENTITY_ID + "/unknownattribute")
											  .param("_method", "PUT")
											  .content("Klaas")
											  .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	public void updateFromFormPost() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post(HREF_ENTITY_ID)
											  .param("_method", "PUT")
											  .param("name", "Klaas")
											  .contentType(MediaType.APPLICATION_FORM_URLENCODED))
			   .andExpect(MockMvcResultMatchers.status().isNoContent());

		Mockito.verify(dataService).update(ArgumentMatchers.eq(ENTITY_NAME), ArgumentMatchers.any(Entity.class));
	}

	@Test
	public void updatePost() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.post(HREF_ENTITY_ID)
											  .param("_method", "PUT")
											  .content("{name:Klaas}")
											  .contentType(MediaType.APPLICATION_JSON))
			   .andExpect(MockMvcResultMatchers.status().isOk());

		Mockito.verify(dataService).update(ArgumentMatchers.eq(ENTITY_NAME), ArgumentMatchers.any(Entity.class));
	}

	@Test
	public void handleUnknownEntityException() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.get(RestController.BASE_URI + "/bogus/1"))
			   .andExpect(MockMvcResultMatchers.status().isNotFound());
	}

	@Test
	public void molgenisDataAccessException() throws Exception
	{
		Mockito.when(dataService.findOneById(ENTITY_NAME, ENTITY_UNTYPED_ID))
			   .thenThrow(new MolgenisDataAccessException());
		mockMvc.perform(MockMvcRequestBuilders.get(HREF_ENTITY_ID))
			   .andExpect(MockMvcResultMatchers.status().isUnauthorized());
	}

	@Test
	public void retrieveEntityCollectionCsv() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.get(RestController.BASE_URI + "/csv/Person")
											  .param("start", "5")
											  .param("num", "10")
											  .param("q", "name==Piet"))
			   .andExpect(MockMvcResultMatchers.status().isOk())
			   .andExpect(MockMvcResultMatchers.content().contentType("text/csv"))
			   .andExpect(MockMvcResultMatchers.content().string(CSV_HEADER + "\"Piet\",\"p1\",\"enum1\",\"1\"\n"));
	}

	@Test
	public void retrieveSortedEntityCollectionCsv() throws Exception
	{
		mockMvc.perform(MockMvcRequestBuilders.get(RestController.BASE_URI + "/csv/Person")
											  .param("sortColumn", "name")
											  .param("sortOrder", "DESC"))
			   .andExpect(MockMvcResultMatchers.status().isOk())
			   .andExpect(MockMvcResultMatchers.content().contentType("text/csv"))
			   .andExpect(MockMvcResultMatchers.content()
											   .string(CSV_HEADER
													   + "\"Klaas\",\"p2\",,\"2\"\n\"Piet\",\"p1\",\"enum1\",\"1\"\n"));
	}

	@Configuration
	public static class RestControllerConfig extends WebMvcConfigurerAdapter
	{
		@Bean
		public AuthenticationSettings authenticationSettings()
		{
			return Mockito.mock(AuthenticationSettings.class);
		}

		@Bean
		public DataService dataService()
		{
			return Mockito.mock(DataService.class);
		}

		@Bean
		public MetaDataService metaDataService()
		{
			return Mockito.mock(MetaDataService.class);
		}

		@Bean
		public TokenService tokenService()
		{
			return Mockito.mock(TokenService.class);
		}

		@Bean
		public AuthenticationManager authenticationManager()
		{
			return Mockito.mock(AuthenticationManager.class);
		}

		@Bean
		public PermissionService permissionService()
		{
			return Mockito.mock(PermissionService.class);
		}

		@Bean
		public UserAccountService userAccountService()
		{
			return Mockito.mock(UserAccountService.class);
		}

		@Bean
		public IdGenerator idGenerator()
		{
			return Mockito.mock(IdGenerator.class);
		}

		@Bean
		public FileStore fileStore()
		{
			return Mockito.mock(FileStore.class);
		}

		@Bean
		public FileMetaFactory fileMetaFactory()
		{
			return Mockito.mock(FileMetaFactory.class);
		}

		@Bean
		public EntityManager entityManager()
		{
			return Mockito.mock(EntityManager.class);
		}

		@Bean
		public ServletUriComponentsBuilderFactory servletUriComponentsBuilderFactory()
		{
			return Mockito.mock(ServletUriComponentsBuilderFactory.class);
		}

		@Bean
		public RestController restController()
		{
			return new RestController(authenticationSettings(), dataService(), tokenService(), authenticationManager(),
					permissionService(), userAccountService(), new MolgenisRSQL(new RSQLParser()),
					new RestService(dataService(), idGenerator(), fileStore(), fileMetaFactory(), entityManager(),
							servletUriComponentsBuilderFactory()));
		}
	}
}
