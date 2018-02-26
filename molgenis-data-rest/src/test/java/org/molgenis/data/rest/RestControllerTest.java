package org.molgenis.data.rest;

import cz.jirutka.rsql.parser.RSQLParser;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.molgenis.core.ui.data.rsql.MolgenisRSQL;
import org.molgenis.core.ui.messageconverter.CsvHttpMessageConverter;
import org.molgenis.core.ui.util.GsonConfig;
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
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.auth.User;
import org.molgenis.data.security.auth.UserMetaData;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.i18n.format.MessageFormatFactory;
import org.molgenis.i18n.test.exception.TestAllPropertiesMessageSource;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.security.core.token.TokenService;
import org.molgenis.security.settings.AuthenticationSettings;
import org.molgenis.security.token.TokenExtractor;
import org.molgenis.security.user.UserAccountService;
import org.molgenis.web.exception.FallbackExceptionHandler;
import org.molgenis.web.exception.GlobalControllerExceptionHandler;
import org.molgenis.web.exception.SpringExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Locale.ENGLISH;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.rest.RestController.BASE_URI;
import static org.molgenis.security.token.TokenExtractor.TOKEN_HEADER;
import static org.molgenis.security.token.TokenExtractor.TOKEN_PARAMETER;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration
@ContextConfiguration(classes = { RestControllerConfig.class, GsonConfig.class })
public class RestControllerTest extends AbstractTestNGSpringContextTests
{
	private static final String ENTITY_NAME = "Person";
	private static final String ENTITY_UNTYPED_ID = "p1";
	private static final String HREF_ENTITY = BASE_URI + '/' + ENTITY_NAME;
	private static final String HREF_ENTITY_META = HREF_ENTITY + "/meta";
	private static final String HREF_ENTITY_ID = HREF_ENTITY + "/p1";
	private static final String FIRST_ERROR_MESSAGE = "$.errors[0].message";
	private static final String FIRST_ERROR_CODE = "$.errors[0].code";

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
	private UserPermissionEvaluator permissionService;

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

	@Mock
	private LocaleResolver localeResolver;

	@BeforeClass
	public void beforeClass()
	{
		ResourceBundleMessageSource validationMessages = new ResourceBundleMessageSource();
		validationMessages.addBasenames("org.hibernate.validator.ValidationMessages");
		TestAllPropertiesMessageSource messageSource = new TestAllPropertiesMessageSource(new MessageFormatFactory());
		messageSource.addMolgenisNamespaces("data", "web");
		messageSource.setParentMessageSource(validationMessages);
		MessageSourceHolder.setMessageSource(messageSource);
	}

	@AfterClass
	public void afterClass()
	{
		MessageSourceHolder.setMessageSource(null);
	}

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		reset(permissionService, dataService, metaDataService, tokenService);

		when(dataService.getMeta()).thenReturn(metaDataService);

		@SuppressWarnings("unchecked")
		Repository<Entity> repo = mock(Repository.class);

		// test entity meta data
		EntityType entityType = mock(EntityType.class);

		Attribute attrId = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		when(attrId.getLabel()).thenReturn("id");
		when(attrId.getLabel("en")).thenReturn("id");
		when(attrId.getDataType()).thenReturn(STRING);
		when(attrId.isReadOnly()).thenReturn(true);
		when(attrId.isUnique()).thenReturn(true);
		when(attrId.isNillable()).thenReturn(false);
		when(attrId.isVisible()).thenReturn(false);
		when(attrId.getChildren()).thenReturn(emptyList());
		when(attrId.getEnumOptions()).thenReturn(emptyList());

		Attribute attrName = when(mock(Attribute.class).getName()).thenReturn("name").getMock();
		when(attrName.getLabel()).thenReturn("name");
		when(attrName.getLabel("en")).thenReturn("name");
		when(attrName.getDataType()).thenReturn(STRING);
		when(attrName.isNillable()).thenReturn(true);
		when(attrName.isVisible()).thenReturn(true);
		when(attrName.getChildren()).thenReturn(emptyList());
		when(attrName.getEnumOptions()).thenReturn(emptyList());

		Attribute attrEnum = when(mock(Attribute.class).getName()).thenReturn("enum").getMock();
		when(attrEnum.getLabel()).thenReturn("enum");
		when(attrEnum.getLabel("en")).thenReturn("enum");
		when(attrEnum.getDataType()).thenReturn(AttributeType.ENUM);
		when(attrEnum.getEnumOptions()).thenReturn(singletonList("enum0, enum1"));
		when(attrEnum.isNillable()).thenReturn(true);
		when(attrEnum.isVisible()).thenReturn(true);
		when(attrEnum.getChildren()).thenReturn(emptyList());

		Attribute attrInt = when(mock(Attribute.class).getName()).thenReturn("int").getMock();
		when(attrInt.getLabel()).thenReturn("int");
		when(attrInt.getLabel("en")).thenReturn("int");
		when(attrInt.getDataType()).thenReturn(AttributeType.INT);
		when(attrInt.isNillable()).thenReturn(true);
		when(attrInt.isVisible()).thenReturn(true);
		when(attrInt.getChildren()).thenReturn(emptyList());

		when(entityType.getAttribute("id")).thenReturn(attrId);
		when(entityType.getAttribute("name")).thenReturn(attrName);
		when(entityType.getAttribute("enum")).thenReturn(attrEnum);
		when(entityType.getAttribute("int")).thenReturn(attrInt);
		when(entityType.getMappedByAttributes()).thenReturn(Stream.empty());
		when(entityType.getIdAttribute()).thenReturn(attrId);
		//TODO: This upgrades the test to mockito 2 but actually shows an error in the test
		when(entityType.getLookupAttributes()).thenReturn(null);
		when(entityType.getAttributes()).thenReturn(asList(attrName, attrId, attrEnum, attrInt));
		when(entityType.getAtomicAttributes()).thenReturn(asList(attrName, attrId, attrEnum, attrInt));
		when(entityType.getId()).thenReturn(ENTITY_NAME);
		when(entityType.getLabel("en")).thenReturn(null);

		when(repo.getEntityType()).thenReturn(entityType);
		when(repo.getName()).thenReturn(ENTITY_NAME);
		when(dataService.getEntityType(ENTITY_NAME)).thenReturn(entityType);
		when(entityManager.create(entityType, POPULATE)).thenReturn(new DynamicEntity(entityType));

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

		when(dataService.getEntityTypeIds()).thenReturn(Stream.of(ENTITY_NAME));
		when(dataService.getRepository(ENTITY_NAME)).thenReturn(repo);

		when(dataService.findOneById(ArgumentMatchers.eq(ENTITY_NAME), ArgumentMatchers.eq(ENTITY_UNTYPED_ID),
				any(Fetch.class))).thenReturn(entity);
		when(dataService.findOneById(ENTITY_NAME, ENTITY_UNTYPED_ID)).thenReturn(entity);

		Query<Entity> q = new QueryImpl<>().eq("name", "Piet").pageSize(10).offset(5);
		when(dataService.findAll(ENTITY_NAME, q)).thenReturn(Stream.of(entity));

		Query<Entity> q2 = new QueryImpl<>().sort(new Sort().on("name", Sort.Direction.DESC)).pageSize(100).offset(0);
		when(dataService.findAll(ENTITY_NAME, q2)).thenReturn(Stream.of(entity2, entity));

		when(localeResolver.resolveLocale(any())).thenReturn(ENGLISH);
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

		Authentication authentication = mock(Authentication.class);
		when(authentication.isAuthenticated()).thenReturn(true);
		when(authentication.getName()).thenReturn(username);
		when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(
				authentication);

		User user = mock(User.class);
		when(user.isChangePassword()).thenReturn(true);
		when(dataService.findOne(UserMetaData.USER, new QueryImpl<User>().eq(UserMetaData.USERNAME, username),
				User.class)).thenReturn(user);

		mockMvc.perform(
				post(BASE_URI + "/login").content(format("{username: '%s', password: '%s'}", username, password))
										 .contentType(APPLICATION_JSON)).andExpect(status().isUnauthorized());
	}

	@Test
	public void testLogoutTokenInHeader() throws Exception
	{
		mockMvc.perform(post(BASE_URI + "/logout").header(TOKEN_HEADER, "abcde").content(""))
			   .andExpect(status().isOk());

		verify(tokenService).removeToken("abcde");
	}

	@Test
	public void testLogoutTokenInParam() throws Exception
	{
		mockMvc.perform(post(BASE_URI + "/logout").param(TOKEN_PARAMETER, "abcde").content(""))
			   .andExpect(status().isOk());
		verify(tokenService).removeToken("abcde");
	}

	@Test
	public void testLogoutNoToken() throws Exception
	{
		mockMvc.perform(post(BASE_URI + "/logout").content(""))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE,
					   is("Missing molgenis token. Token should either be present in the x-molgenis-token request header or the molgenis-token parameter.")));
		verifyZeroInteractions(tokenService);
	}

	@Test
	public void create() throws Exception
	{
		mockMvc.perform(post(HREF_ENTITY).content("{id:'p1', name:'Piet'}").contentType(APPLICATION_JSON))
			   .andExpect(status().isCreated())
			   .andExpect(MockMvcResultMatchers.header().string("Location", HREF_ENTITY_ID));

		verify(dataService).add(ArgumentMatchers.eq(ENTITY_NAME), any(Entity.class));
	}

	@Test
	public void createFromFormPost() throws Exception
	{
		mockMvc.perform(post(HREF_ENTITY).contentType(MediaType.APPLICATION_FORM_URLENCODED)
										 .param("id", "p1")
										 .param("name", "Piet")).andExpect(status().isCreated())
			   .andExpect(MockMvcResultMatchers.header().string("Location", HREF_ENTITY_ID));

		verify(dataService).add(ArgumentMatchers.eq(ENTITY_NAME), any(Entity.class));
	}

	@Test
	public void deleteDelete() throws Exception
	{
		mockMvc.perform(delete(HREF_ENTITY_ID)).andExpect(status().isNoContent());
		verify(dataService).deleteById(ENTITY_NAME, ENTITY_UNTYPED_ID);
	}

	@Test
	public void deletePost() throws Exception
	{
		mockMvc.perform(post(HREF_ENTITY_ID).param("_method", "DELETE")).andExpect(status().isNoContent());
		verify(dataService).deleteById(ENTITY_NAME, ENTITY_UNTYPED_ID);
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
		verify(metaDataService).deleteEntityType(ENTITY_NAME);
	}

	@Test
	public void deleteMetaPost() throws Exception
	{
		mockMvc.perform(post(HREF_ENTITY_META).param("_method", "DELETE")).andExpect(status().isNoContent());
		verify(metaDataService).deleteEntityType(ENTITY_NAME);
	}

	@Test
	public void retrieveEntityType() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_META))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().string(ENTITY_META_RESPONSE_STRING));
	}

	@Test
	public void retrieveEntityTypeWritable() throws Exception
	{
		when(permissionService.hasPermission(new EntityTypeIdentity(ENTITY_NAME),
				EntityTypePermission.WRITE)).thenReturn(true);
		when(dataService.getCapabilities(ENTITY_NAME)).thenReturn(
				new HashSet<>(singletonList(RepositoryCapability.WRITABLE)));
		mockMvc.perform(get(HREF_ENTITY_META))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(
					   "{\"href\":\"" + HREF_ENTITY_META + "\",\"hrefCollection\":\"/api/v1/Person\",\"name\":\""
							   + ENTITY_NAME + "\",\"attributes\":{\"name\":{\"href\":\"" + HREF_ENTITY_META
							   + "/name\"},\"id\":{\"href\":\"/api/v1/Person/meta/id\"},\"enum\":{\"href\":\"/api/v1/Person/meta/enum\"},\"int\":{\"href\":\"/api/v1/Person/meta/int\"}},\"idAttribute\":\"id\",\"isAbstract\":false,\"languageCode\":\"en\",\"writable\":true}"));
	}

	@Test
	public void retrieveEntityTypeNotWritable() throws Exception
	{
		when(permissionService.hasPermission(new EntityTypeIdentity(ENTITY_NAME),
				EntityTypePermission.WRITE)).thenReturn(true);
		when(dataService.getCapabilities(ENTITY_NAME)).thenReturn(
				new HashSet<>(singletonList(RepositoryCapability.QUERYABLE)));
		mockMvc.perform(get(HREF_ENTITY_META))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(ENTITY_META_RESPONSE_STRING));
	}

	@Test
	public void retrieveEntityTypePost() throws Exception
	{
		String json = "{\"attributes\":[\"name\"]}";
		mockMvc.perform(post(HREF_ENTITY_META).param("_method", "GET").content(json).contentType(APPLICATION_JSON))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(
					   "{\"href\":\"" + HREF_ENTITY_META + "\",\"hrefCollection\":\"/api/v1/Person\",\"name\":\""
							   + ENTITY_NAME + "\",\"languageCode\":\"en\",\"writable\":false}"));
	}

	@Test
	public void retrieveEntityTypeSelectAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_META).param("attributes", "name"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(
					   "{\"href\":\"" + HREF_ENTITY_META + "\",\"hrefCollection\":\"/api/v1/Person\",\"name\":\""
							   + ENTITY_NAME + "\",\"languageCode\":\"en\",\"writable\":false}"));
	}

	@Test
	public void retrieveEntityTypeExpandAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_META).param("expand", "attributes"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(
					   "{\"href\":\"/api/v1/Person/meta\",\"hrefCollection\":\"/api/v1/Person\",\"name\":\"Person\",\"attributes\":{\"name\":{\"href\":\"/api/v1/Person/meta/name\",\"fieldType\":\"STRING\",\"name\":\"name\",\"label\":\"name\",\"attributes\":[],\"enumOptions\":[],\"maxLength\":255,\"auto\":false,\"nillable\":true,\"readOnly\":false,\"labelAttribute\":false,\"unique\":false,\"visible\":true,\"lookupAttribute\":false,\"isAggregatable\":false},\"id\":{\"href\":\"/api/v1/Person/meta/id\",\"fieldType\":\"STRING\",\"name\":\"id\",\"label\":\"id\",\"attributes\":[],\"enumOptions\":[],\"maxLength\":255,\"auto\":false,\"nillable\":false,\"readOnly\":true,\"labelAttribute\":false,\"unique\":true,\"visible\":false,\"lookupAttribute\":false,\"isAggregatable\":false},\"enum\":{\"href\":\"/api/v1/Person/meta/enum\",\"fieldType\":\"ENUM\",\"name\":\"enum\",\"label\":\"enum\",\"attributes\":[],\"enumOptions\":[\"enum0, enum1\"],\"maxLength\":255,\"auto\":false,\"nillable\":true,\"readOnly\":false,\"labelAttribute\":false,\"unique\":false,\"visible\":true,\"lookupAttribute\":false,\"isAggregatable\":false},\"int\":{\"href\":\"/api/v1/Person/meta/int\",\"fieldType\":\"INT\",\"name\":\"int\",\"label\":\"int\",\"attributes\":[],\"enumOptions\":[],\"auto\":false,\"nillable\":true,\"readOnly\":false,\"labelAttribute\":false,\"unique\":false,\"visible\":true,\"lookupAttribute\":false,\"isAggregatable\":false}},\"idAttribute\":\"id\",\"isAbstract\":false,\"languageCode\":\"en\",\"writable\":false}"));
	}

	@Test
	public void retrieve() throws Exception
	{
		restController.retrieveEntity(ENTITY_NAME, ENTITY_UNTYPED_ID, new String[] {}, new String[] {});

		mockMvc.perform(get(HREF_ENTITY_ID))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json("{\"href\":\"" + HREF_ENTITY_ID
					   + "\",\"name\":\"Piet\",\"id\":\"p1\",\"enum\":\"enum1\",\"int\":1}"));

	}

	@Test
	public void retrieveSelectAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attributes", "notname"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json("{\"href\":\"" + HREF_ENTITY_ID + "\"}"));

	}

	@Test
	public void retrieveEntityCollection() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY).param("start", "5")
										.param("num", "10")
										.param("q[0].operator", "EQUALS")
										.param("q[0].field", "name")
										.param("q[0].value", "Piet"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(ENTITY_COLLECTION_RESPONSE_STRING));

	}

	@Test
	public void retrieveEntityCollectionPost() throws Exception
	{

		mockMvc.perform(post(HREF_ENTITY).param("_method", "GET")
										 .content("{start:5, num:10, q:[{operator:EQUALS,field:name,value:Piet}]}")
										 .contentType(APPLICATION_JSON))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(ENTITY_COLLECTION_RESPONSE_STRING));
	}

	@Test
	public void retrieveEntityAttribute() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID + "/name"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json("{\"href\":\"" + HREF_ENTITY_ID + "/name\",\"name\":\"Piet\"}"));
	}

	@Test
	public void retrieveAttributeUnknownEntity() throws Exception
	{
		String HREF_UNKNOWN_ENTITY_META = BASE_URI + "/unknown/meta";
		mockMvc.perform(get(HREF_UNKNOWN_ENTITY_META + "/attribute")).andExpect(status().isNotFound());
	}

	@Test
	public void retrieveEntityAttributePost() throws Exception
	{
		String json = "{\"attributes\":[\"name\"]}";
		mockMvc.perform(
				post(HREF_ENTITY_ID + "/name").param("_method", "GET").content(json).contentType(APPLICATION_JSON))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json("{\"href\":\"" + HREF_ENTITY_ID + "/name\",\"name\":\"Piet\"}"));
	}

	@Test
	public void retrieveEntityAttributeUnknownAttribute() throws Exception
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> repo = mock(Repository.class);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn(ENTITY_NAME);
		when(entityType.getLabel("en")).thenReturn("My Entity");
		when(entityType.getAttribute("name")).thenReturn(null);
		Attribute idAttr = when(mock(Attribute.class).getDataType()).thenReturn(STRING).getMock();
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(repo.getEntityType()).thenReturn(entityType);
		when(dataService.getEntityType(ENTITY_NAME)).thenReturn(entityType);
		when(dataService.getRepository(ENTITY_NAME)).thenReturn(repo);
		mockMvc.perform(get(HREF_ENTITY_ID + "/name"))
			   .andExpect(status().isNotFound())
			   .andExpect(jsonPath(FIRST_ERROR_CODE, is("D04")))
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("Unknown attribute 'name' of entity type 'My Entity'.")));
	}

	@Test
	public void retrieveEntityAttributeUnknownEntity() throws Exception
	{
		EntityType entityType = mock(EntityType.class);
		when(entityType.getLabel("en")).thenReturn("My entity type");
		when(dataService.getEntityType(ENTITY_NAME)).thenReturn(entityType);
		Attribute idAttribute = mock(Attribute.class);
		when(entityType.getIdAttribute()).thenReturn(idAttribute);
		when(idAttribute.getDataType()).thenReturn(STRING);
		when(dataService.findOneById(ENTITY_NAME, ENTITY_UNTYPED_ID)).thenReturn(null);
		mockMvc.perform(get(HREF_ENTITY_ID + "/name"))
			   .andExpect(status().isNotFound())
			   .andExpect(jsonPath(FIRST_ERROR_CODE, is("D04")))
			   .andExpect(
					   jsonPath(FIRST_ERROR_MESSAGE, is("Unknown attribute 'name' of entity type 'My entity type'.")));
	}

	@Test
	public void retrieveEntityAttributeXref() throws Exception
	{
		reset(dataService);

		@SuppressWarnings("unchecked")
		Repository<Entity> repo = mock(Repository.class);
		when(dataService.getRepository(ENTITY_NAME)).thenReturn(repo);
		when(dataService.getEntityTypeIds()).thenReturn(Stream.of(ENTITY_NAME));

		// entity meta data
		EntityType refEntityType = when(mock(EntityType.class).getId()).thenReturn("refEntity").getMock();

		Attribute attrId = when(mock(Attribute.class).getName()).thenReturn("id").getMock();
		when(attrId.getLabel()).thenReturn("id");
		when(attrId.getLabel(ArgumentMatchers.anyString())).thenReturn("id");
		when(attrId.getDataType()).thenReturn(STRING);
		when(attrId.isReadOnly()).thenReturn(true);
		when(attrId.isUnique()).thenReturn(true);
		when(attrId.isNillable()).thenReturn(false);
		when(attrId.isVisible()).thenReturn(false);
		when(attrId.getChildren()).thenReturn(emptyList());
		when(attrId.getEnumOptions()).thenReturn(emptyList());

		Attribute attrName = when(mock(Attribute.class).getName()).thenReturn("name").getMock();
		when(attrName.getLabel()).thenReturn("name");
		when(attrName.getLabel(ArgumentMatchers.anyString())).thenReturn("name");
		when(attrName.getDataType()).thenReturn(STRING);
		when(attrName.isNillable()).thenReturn(true);
		when(attrName.isVisible()).thenReturn(true);
		when(attrName.getChildren()).thenReturn(emptyList());
		when(attrName.getEnumOptions()).thenReturn(emptyList());

		when(refEntityType.getAttribute("id")).thenReturn(attrId);
		when(refEntityType.getAttribute("name")).thenReturn(attrName);
		when(refEntityType.getIdAttribute()).thenReturn(attrId);
		when(refEntityType.getAttributes()).thenReturn(asList(attrId, attrName));
		when(refEntityType.getAtomicAttributes()).thenReturn(asList(attrId, attrName));
		when(refEntityType.getId()).thenReturn("refEntity");

		EntityType entityType = when(mock(EntityType.class).getId()).thenReturn(ENTITY_NAME).getMock();

		Attribute attrXref = when(mock(Attribute.class).getName()).thenReturn("xrefValue").getMock();
		when(attrXref.getLabel()).thenReturn("xrefValue");
		when(attrXref.getLabel(ArgumentMatchers.anyString())).thenReturn("xrefValue");
		when(attrXref.getDataType()).thenReturn(AttributeType.XREF);
		when(attrXref.isNillable()).thenReturn(true);
		when(attrXref.isVisible()).thenReturn(true);
		when(attrXref.getChildren()).thenReturn(emptyList());
		when(attrXref.getEnumOptions()).thenReturn(emptyList());
		when(attrXref.getRefEntity()).thenReturn(refEntityType);

		when(entityType.getAttribute("id")).thenReturn(attrId);
		when(entityType.getAttribute("xrefValue")).thenReturn(attrXref);
		when(entityType.getIdAttribute()).thenReturn(attrId);
		when(entityType.getAttributes()).thenReturn(asList(attrId, attrXref));
		when(entityType.getAtomicAttributes()).thenReturn(asList(attrId, attrXref));
		when(entityType.getId()).thenReturn(ENTITY_NAME);

		Entity entityXref = new DynamicEntity(refEntityType);
		entityXref.set("id", ENTITY_UNTYPED_ID);
		entityXref.set("name", "Piet");

		Entity entity = new DynamicEntity(entityType);
		entity.set("id", ENTITY_UNTYPED_ID);
		entity.set("xrefValue", entityXref);

		when(dataService.findOneById(ENTITY_NAME, ENTITY_UNTYPED_ID)).thenReturn(entity);
		when(dataService.findOneById("refEntity", ENTITY_UNTYPED_ID)).thenReturn(entityXref);
		when(dataService.getEntityType(ENTITY_NAME)).thenReturn(entityType);
		when(dataService.getEntityType("refEntity")).thenReturn(refEntityType);
		mockMvc = MockMvcBuilders.standaloneSetup(restController)
								 .setMessageConverters(gsonHttpMessageConverter)
								 .build();

		mockMvc.perform(get(HREF_ENTITY_ID + "/xrefValue"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(
					   content().string("{\"href\":\"/api/v1/Person/p1/xrefValue\",\"id\":\"p1\",\"name\":\"Piet\"}"));
	}

	@Test
	public void update() throws Exception
	{
		mockMvc.perform(
				MockMvcRequestBuilders.put(HREF_ENTITY_ID).content("{name:Klaas}").contentType(APPLICATION_JSON))
			   .andExpect(status().isOk());

		verify(dataService).update(ArgumentMatchers.eq(ENTITY_NAME), any(Entity.class));
	}

	@Test
	public void updateInternalRepoNotUpdateable() throws Exception
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> repo = mock(Repository.class);
		when(dataService.getRepository(ENTITY_NAME)).thenReturn(repo);
		Mockito.doThrow(new MolgenisDataException())
			   .when(dataService)
			   .update(ArgumentMatchers.anyString(), any(Entity.class));
		mockMvc.perform(
				MockMvcRequestBuilders.put(HREF_ENTITY_ID).content("{name:Klaas}").contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest());
	}

	@Test
	public void updateInternalRepoIdAttributeIsNull() throws Exception
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> repo = mock(Repository.class);
		when(dataService.getRepository(ENTITY_NAME)).thenReturn(repo);
		EntityType entityType = mock(EntityType.class);
		when(entityType.getIdAttribute()).thenReturn(null);
		when(repo.getEntityType()).thenReturn(entityType);
		when(dataService.getEntityType(ENTITY_NAME)).thenReturn(entityType);
		mockMvc.perform(
				MockMvcRequestBuilders.put(HREF_ENTITY_ID).content("{name:Klaas}").contentType(APPLICATION_JSON))
			   .andExpect(status().isInternalServerError());
	}

	@Test
	public void updateInternalRepoExistingIsNull() throws Exception
	{
		when(dataService.findOneById(ArgumentMatchers.eq(ENTITY_NAME), ArgumentMatchers.eq(ENTITY_UNTYPED_ID),
				any(Fetch.class))).thenReturn(null);

		mockMvc.perform(
				MockMvcRequestBuilders.put(HREF_ENTITY_ID).content("{name:Klaas}").contentType(APPLICATION_JSON))
			   .andExpect(status().isNotFound());
	}

	@Test
	public void updateAttribute() throws Exception
	{
		mockMvc.perform(
				post(HREF_ENTITY_ID + "/name").param("_method", "PUT").content("Klaas").contentType(APPLICATION_JSON))
			   .andExpect(status().isOk());
		verify(dataService).update(ArgumentMatchers.eq(ENTITY_NAME), any(Entity.class));
	}

	@Test
	public void updateAttribute_unknownEntity() throws Exception
	{
		mockMvc.perform(post(BASE_URI + "/unknownentity/" + ENTITY_UNTYPED_ID + "/name").param("_method", "PUT")
																						.content("Klaas")
																						.contentType(APPLICATION_JSON))
			   .andExpect(status().isNotFound());
	}

	@Test
	public void updateAttribute_unknownEntityId() throws Exception
	{
		mockMvc.perform(post(HREF_ENTITY + "/666" + "/name").param("_method", "PUT")
															.content("Klaas")
															.contentType(APPLICATION_JSON))
			   .andExpect(status().isNotFound());
	}

	@Test
	public void updateAttribute_unknownAttribute() throws Exception
	{
		mockMvc.perform(post(HREF_ENTITY_ID + "/unknownattribute").param("_method", "PUT")
																  .content("Klaas")
																  .contentType(APPLICATION_JSON))
			   .andExpect(status().isNotFound());
	}

	@Test
	public void updateFromFormPost() throws Exception
	{
		mockMvc.perform(post(HREF_ENTITY_ID).param("_method", "PUT")
											.param("name", "Klaas")
											.contentType(MediaType.APPLICATION_FORM_URLENCODED))
			   .andExpect(status().isNoContent());

		verify(dataService).update(ArgumentMatchers.eq(ENTITY_NAME), any(Entity.class));
	}

	@Test
	public void updatePost() throws Exception
	{
		mockMvc.perform(
				post(HREF_ENTITY_ID).param("_method", "PUT").content("{name:Klaas}").contentType(APPLICATION_JSON))
			   .andExpect(status().isOk());

		verify(dataService).update(ArgumentMatchers.eq(ENTITY_NAME), any(Entity.class));
	}

	@Test
	public void handleUnknownEntityException() throws Exception
	{
		mockMvc.perform(get(BASE_URI + "/bogus/1")).andExpect(status().isNotFound());
	}

	@Test
	public void molgenisDataAccessException() throws Exception
	{
		when(dataService.findOneById(ENTITY_NAME, ENTITY_UNTYPED_ID)).thenThrow(new MolgenisDataAccessException());
		mockMvc.perform(get(HREF_ENTITY_ID)).andExpect(status().isUnauthorized());
	}

	@Test
	public void retrieveEntityCollectionCsv() throws Exception
	{
		mockMvc.perform(get(BASE_URI + "/csv/Person").param("start", "5").param("num", "10").param("q", "name==Piet"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType("text/csv"))
			   .andExpect(content().string(CSV_HEADER + "\"Piet\",\"p1\",\"enum1\",\"1\"\n"));
	}

	@Test
	public void retrieveSortedEntityCollectionCsv() throws Exception
	{
		mockMvc.perform(get(BASE_URI + "/csv/Person").param("sortColumn", "name").param("sortOrder", "DESC"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType("text/csv"))
			   .andExpect(content().string(CSV_HEADER + "\"Klaas\",\"p2\",,\"2\"\n\"Piet\",\"p1\",\"enum1\",\"1\"\n"));
	}

	@Configuration
	public static class RestControllerConfig extends WebMvcConfigurerAdapter
	{
		@Bean
		public AuthenticationSettings authenticationSettings()
		{
			return mock(AuthenticationSettings.class);
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public MetaDataService metaDataService()
		{
			return mock(MetaDataService.class);
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
		public UserPermissionEvaluator permissionService()
		{
			return mock(UserPermissionEvaluator.class);
		}

		@Bean
		public UserAccountService userAccountService()
		{
			return mock(UserAccountService.class);
		}

		@Bean
		public IdGenerator idGenerator()
		{
			return mock(IdGenerator.class);
		}

		@Bean
		public FileStore fileStore()
		{
			return mock(FileStore.class);
		}

		@Bean
		public FileMetaFactory fileMetaFactory()
		{
			return mock(FileMetaFactory.class);
		}

		@Bean
		public EntityManager entityManager()
		{
			return mock(EntityManager.class);
		}

		@Bean
		public ServletUriComponentsBuilderFactory servletUriComponentsBuilderFactory()
		{
			return mock(ServletUriComponentsBuilderFactory.class);
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
