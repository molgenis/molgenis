package org.molgenis.data.rest.v2;

import com.google.common.collect.Sets;
import org.mockito.ArgumentCaptor;
import org.mockito.quality.Strictness;
import org.molgenis.core.ui.util.GsonConfig;
import org.molgenis.data.*;
import org.molgenis.data.file.FileStore;
import org.molgenis.data.file.model.FileMetaFactory;
import org.molgenis.data.i18n.LocalizationService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.MetaDataService;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.rest.service.RestService;
import org.molgenis.data.rest.service.ServletUriComponentsBuilderFactory;
import org.molgenis.data.rest.v2.RestControllerV2Test.RestControllerV2Config;
import org.molgenis.data.security.EntityTypeIdentity;
import org.molgenis.data.security.EntityTypePermission;
import org.molgenis.data.security.permission.PermissionSystemService;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.support.RepositoryCopier;
import org.molgenis.data.util.MolgenisDateFormat;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.i18n.MessageSourceHolder;
import org.molgenis.i18n.format.MessageFormatFactory;
import org.molgenis.i18n.test.exception.TestAllPropertiesMessageSource;
import org.molgenis.security.core.UserPermissionEvaluator;
import org.molgenis.web.exception.FallbackExceptionHandler;
import org.molgenis.web.exception.GlobalControllerExceptionHandler;
import org.molgenis.web.exception.SpringExceptionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.http.converter.json.GsonHttpMessageConverter;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.testng.annotations.*;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.molgenis.data.EntityManager.CreationMode.POPULATE;
import static org.molgenis.data.meta.AttributeType.*;
import static org.molgenis.data.meta.model.AttributeMetadata.ATTRIBUTE_META_DATA;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.*;
import static org.molgenis.data.rest.v2.RestControllerV2.BASE_URI;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testng.Assert.assertEquals;
import static org.testng.reporters.Files.readFile;

@WebAppConfiguration
@ContextConfiguration(classes = { RestControllerV2Config.class, GsonConfig.class })
public class RestControllerV2Test extends AbstractMolgenisSpringTest
{
	private static final String SELF_REF_ENTITY_NAME = "selfRefEntity";
	private static final String ENTITY_NAME = "entity";
	private static final String REF_ENTITY_NAME = "refEntity";
	private static final String REF_REF_ENTITY_NAME = "refRefEntity";

	private static final String REF_ATTR_ID_NAME = "id";
	private static final String REF_ATTR_VALUE_NAME = "value";
	private static final String REF_ATTR_REF_NAME = "ref";
	private static final String REF_REF_ATTR_VALUE_NAME = "value";
	private static final String REF_REF_ATTR_ID_NAME = "id";

	private static final String ENTITY_ID = "0";
	private static final String REF_ENTITY0_ID = "ref0";
	private static final String REF_ENTITY1_ID = "ref1";
	private static final String REF_ENTITY0_LABEL = "label0";
	private static final String REF_ENTITY1_LABEL = "label1";
	private static final String REF_REF_ENTITY_ID = "refRef0";
	private static final String HREF_ENTITY_COLLECTION = BASE_URI + '/' + ENTITY_NAME;
	private static final String HREF_ENTITY_COLLECTION_INCLUDE_CATEGORIES_IS_TRUE =
			BASE_URI + '/' + ENTITY_NAME + "?includeCategories=true";
	private static final String HREF_COPY_ENTITY = BASE_URI + "/copy/" + ENTITY_NAME;
	private static final String HREF_ENTITY_ID = HREF_ENTITY_COLLECTION + '/' + ENTITY_ID;
	private static final String HREF_ENTITY_ID_INCLUDE_CATEGORIES_IS_TRUE =
			HREF_ENTITY_COLLECTION + '/' + ENTITY_ID + "?includeCategories=true";
	private static final String FIRST_ERROR_MESSAGE = "$.errors[0].message";
	private static final String FIRST_ERROR_CODE = "$.errors[0].code";

	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attributeFactory;

	@Autowired
	private RestControllerV2 restControllerV2;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private FormattingConversionService conversionService;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@Autowired
	private UserPermissionEvaluator permissionService;

	@Autowired
	private PermissionSystemService permissionSystemService;

	@Autowired
	private RepositoryCopier repoCopier;

	@Autowired
	private DataService dataService;

	@Autowired
	private LocaleResolver localeResolver;

	private MockMvc mockMvc;
	private String attrBoolName;
	private String attrStringName;
	private String attrXrefName;
	private String attrCompoundName;
	private String attrCompoundAttr0Name;
	private String attrCompoundCategorical;
	private String attrCompoundAttrCompoundName;
	private String attrCompoundAttrCompoundAttr0Name;
	private EntityType entityType;

	public RestControllerV2Test()
	{
		super(Strictness.WARN);
	}

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
		reset(dataService);
		reset(repoCopier);

		EntityType refRefEntityType = entityTypeFactory.create(REF_REF_ENTITY_NAME)
													   .setLabel(REF_REF_ENTITY_NAME)
													   .addAttribute(
															   attributeFactory.create().setName(REF_REF_ATTR_ID_NAME),
															   ROLE_ID, ROLE_LOOKUP)
													   .addAttribute(attributeFactory.create()
																					 .setName(REF_REF_ATTR_VALUE_NAME),
															   ROLE_LABEL);

		EntityType selfRefEntityType = entityTypeFactory.create(SELF_REF_ENTITY_NAME)
														.setLabel(SELF_REF_ENTITY_NAME)
														.addAttribute(attributeFactory.create().setName("id"), ROLE_ID,
																ROLE_LABEL, ROLE_LOOKUP);
		selfRefEntityType.addAttribute(
				attributeFactory.create().setName("selfRef").setDataType(XREF).setRefEntity(selfRefEntityType));

		Entity selfRefEntity = new DynamicEntity(selfRefEntityType);
		selfRefEntity.set("id", "0");
		selfRefEntity.set("selfRef", selfRefEntity);

		EntityType refEntityType = entityTypeFactory.create(REF_ENTITY_NAME)
													.setLabel(REF_ENTITY_NAME)
													.addAttribute(attributeFactory.create().setName(REF_ATTR_ID_NAME),
															ROLE_ID, ROLE_LOOKUP)
													.addAttribute(
															attributeFactory.create().setName(REF_ATTR_VALUE_NAME),
															ROLE_LABEL)
													.addAttribute(attributeFactory.create()
																				  .setName(REF_ATTR_REF_NAME)
																				  .setDataType(XREF)
																				  .setRefEntity(refRefEntityType));

		// required
		String attrIdName = "id";
		attrBoolName = "bool";
		String attrCategoricalName = "categorical";
		String attrCategoricalMrefName = "categorical_mref";
		attrCompoundName = "compound";
		attrCompoundAttr0Name = "compound_attr0";
		attrCompoundCategorical = "compound_categorical";
		attrCompoundAttrCompoundName = "compound_attrcompound";
		attrCompoundAttrCompoundAttr0Name = "compound_attrcompound_attr0";
		String attrDateName = "date";
		String attrDateTimeName = "date_time";
		String attrDecimalName = "decimal";
		String attrEmailName = "email";
		String attrEnumName = "enum";
		String attrHtmlName = "html";
		String attrHyperlinkName = "hyperlink";
		String attrIntName = "int";
		String attrLongName = "long";
		String attrMrefName = "mref";
		String attrScriptName = "script";
		attrStringName = "string";
		String attrTextName = "text";
		attrXrefName = "xref";

		// optional
		String attrBoolOptionalName = "boolOptional";
		String attrCategoricalOptionalName = "categoricalOptional";
		String attrCategoricalMrefOptionalName = "categorical_mrefOptional";
		String attrCompoundAttr0OptionalName = "compound_attr0Optional";
		String attrCompoundAttrCompoundAttr0OptionalName = "compound_attrcompound_attr0Optional";
		String attrDateOptionalName = "dateOptional";
		String attrDateTimeOptionalName = "date_timeOptional";
		String attrDecimalOptionalName = "decimalOptional";
		String attrEmailOptionalName = "emailOptional";
		String attrEnumOptionalName = "enumOptional";
		String attrHtmlOptionalName = "htmlOptional";
		String attrHyperlinkOptionalName = "hyperlinkOptional";
		String attrIntOptionalName = "intOptional";
		String attrLongOptionalName = "longOptional";
		String attrMrefOptionalName = "mrefOptional";
		String attrScriptOptionalName = "scriptOptional";
		String attrStringOptionalName = "stringOptional";
		String attrTextOptionalName = "textOptional";
		String attrXrefOptionalName = "xrefOptional";

		String enum0 = "enum0";
		String enum1 = "enum1";
		String enum2 = "enum2";

		// required
		entityType = entityTypeFactory.create(ENTITY_NAME).setLabel(ENTITY_NAME);
		Attribute attrId = attributeFactory.create().setName(attrIdName);
		entityType.addAttribute(attrId, ROLE_ID, ROLE_LABEL, ROLE_LOOKUP);
		Attribute attrBool = createAttributeMeta(entityType, attrBoolName, BOOL).setNillable(false);
		Attribute attrCategorical = createAttributeMeta(entityType, attrCategoricalName, CATEGORICAL,
				refEntityType).setNillable(false);
		Attribute attrCategoricalMref = createAttributeMeta(entityType, attrCategoricalMrefName, CATEGORICAL_MREF,
				refEntityType).setNillable(false);
		Attribute attrCompound = createAttributeMeta(entityType, attrCompoundName, COMPOUND);
		Attribute compoundAttr0 = createAttributeMeta(entityType, attrCompoundAttr0Name, STRING).setNillable(false).setParent(attrCompound);
		Attribute compoundAttrCategorical = createAttributeMeta(entityType, attrCompoundCategorical, CATEGORICAL,
				refEntityType).setNillable(false).setParent(attrCompound);
		Attribute compoundAttr0Optional = createAttributeMeta(entityType, attrCompoundAttr0OptionalName,
				STRING).setNillable(true).setParent(attrCompound);
		Attribute compoundAttrCompound = createAttributeMeta(entityType, attrCompoundAttrCompoundName,
				COMPOUND).setParent(attrCompound);
		Attribute compoundAttrCompoundAttr0 = createAttributeMeta(entityType, attrCompoundAttrCompoundAttr0Name, STRING)
				.setNillable(false)
				.setParent(compoundAttrCompound);
		Attribute compoundAttrCompoundAttr0Optional = createAttributeMeta(entityType,
				attrCompoundAttrCompoundAttr0OptionalName, STRING).setNillable(true).setParent(compoundAttrCompound);
		Attribute attrDate = createAttributeMeta(entityType, attrDateName, DATE).setNillable(false);
		Attribute attrDateTime = createAttributeMeta(entityType, attrDateTimeName, DATE_TIME).setNillable(false);
		Attribute attrDecimal = createAttributeMeta(entityType, attrDecimalName, DECIMAL, null).setReadOnly(true)
																							   .setNillable(false);
		Attribute attrEmail = createAttributeMeta(entityType, attrEmailName, EMAIL).setNillable(false);
		Attribute attrEnum = createAttributeMeta(entityType, attrEnumName, ENUM).setEnumOptions(
				asList(enum0, enum1, enum2)).setNillable(false);
		Attribute attrHtml = createAttributeMeta(entityType, attrHtmlName, HTML).setNillable(false);
		Attribute attrHyperlink = createAttributeMeta(entityType, attrHyperlinkName, HYPERLINK).setNillable(false);
		Attribute attrInt = createAttributeMeta(entityType, attrIntName, INT).setNillable(false);
		Attribute attrLong = createAttributeMeta(entityType, attrLongName, LONG).setNillable(false);
		Attribute attrMref = createAttributeMeta(entityType, attrMrefName, MREF, refEntityType).setNillable(false);
		Attribute attrScript = createAttributeMeta(entityType, attrScriptName, SCRIPT).setNillable(false);
		Attribute attrString = createAttributeMeta(entityType, attrStringName, STRING).setNillable(false);
		Attribute attrText = createAttributeMeta(entityType, attrTextName, TEXT).setNillable(false);
		Attribute attrXref = createAttributeMeta(entityType, attrXrefName, XREF, refEntityType).setNillable(false);

		// optional
		Attribute attrBoolOptional = createAttributeMeta(entityType, attrBoolOptionalName, BOOL);
		Attribute attrCategoricalOptional = createAttributeMeta(entityType, attrCategoricalOptionalName, CATEGORICAL,
				refEntityType);
		Attribute attrCategoricalMrefOptional = createAttributeMeta(entityType, attrCategoricalMrefOptionalName,
				CATEGORICAL_MREF, refEntityType);
		Attribute attrDateOptional = createAttributeMeta(entityType, attrDateOptionalName, DATE);
		Attribute attrDateTimeOptional = createAttributeMeta(entityType, attrDateTimeOptionalName, DATE_TIME);
		Attribute attrDecimalOptional = createAttributeMeta(entityType, attrDecimalOptionalName, DECIMAL, null);
		Attribute attrEmailOptional = createAttributeMeta(entityType, attrEmailOptionalName, EMAIL);
		Attribute attrEnumOptional = createAttributeMeta(entityType, attrEnumOptionalName, ENUM).setEnumOptions(
				asList(enum0, enum1, enum2));
		Attribute attrHtmlOptional = createAttributeMeta(entityType, attrHtmlOptionalName, HTML);
		Attribute attrHyperlinkOptional = createAttributeMeta(entityType, attrHyperlinkOptionalName, HYPERLINK);
		Attribute attrIntOptional = createAttributeMeta(entityType, attrIntOptionalName, INT);
		Attribute attrLongOptional = createAttributeMeta(entityType, attrLongOptionalName, LONG);
		Attribute attrMrefOptional = createAttributeMeta(entityType, attrMrefOptionalName, MREF, refEntityType);
		Attribute attrScriptOptional = createAttributeMeta(entityType, attrScriptOptionalName, SCRIPT);
		Attribute attrStringOptional = createAttributeMeta(entityType, attrStringOptionalName, STRING);
		Attribute attrTextOptional = createAttributeMeta(entityType, attrTextOptionalName, TEXT);
		Attribute attrXrefOptional = createAttributeMeta(entityType, attrXrefOptionalName, XREF, refEntityType);

		Entity refRefEntity = new DynamicEntity(refRefEntityType);
		refRefEntity.set(REF_REF_ATTR_ID_NAME, REF_REF_ENTITY_ID);
		refRefEntity.set(REF_REF_ATTR_VALUE_NAME, "value");

		Entity refEntity0 = new DynamicEntity(refEntityType);
		refEntity0.set(REF_ATTR_ID_NAME, REF_ENTITY0_ID);
		refEntity0.set(REF_ATTR_VALUE_NAME, REF_ENTITY0_LABEL);
		refEntity0.set(REF_ATTR_REF_NAME, refRefEntity);

		Entity refEntity1 = new DynamicEntity(refEntityType);
		refEntity1.set(REF_ATTR_ID_NAME, REF_ENTITY1_ID);
		refEntity1.set(REF_ATTR_VALUE_NAME, REF_ENTITY1_LABEL);
		refEntity1.set(REF_ATTR_REF_NAME, refRefEntity);

		Entity entity = new DynamicEntity(entityType);

		// required
		entity.set(attrIdName, ENTITY_ID);
		entity.set(attrBoolName, true);
		entity.set(attrCategoricalName, refEntity0);
		entity.set(attrCategoricalMrefName, asList(refEntity0, refEntity1));
		entity.set(attrCompoundAttr0Name, "compoundAttr0Str");
		entity.set(attrCompoundCategorical, refEntity0);
		entity.set(attrCompoundAttrCompoundAttr0Name, "compoundAttrCompoundAttr0Str");
		entity.set(attrDateName, LocalDate.parse("2015-05-22"));
		entity.set(attrDateTimeName, Instant.parse("2015-05-22T06:12:13Z"));
		entity.set(attrDecimalName, 3.14);
		entity.set(attrEmailName, "my@mail.com");
		entity.set(attrEnumName, enum0);
		entity.set(attrHtmlName, "<h1>html</h1>");
		entity.set(attrHyperlinkName, "http://www.molgenis.org/");
		entity.set(attrIntName, 123);
		entity.set(attrLongName, Long.MAX_VALUE);
		entity.set(attrMrefName, asList(refEntity0, refEntity1));
		entity.set(attrScriptName, "print \"Hello world\"");
		entity.set(attrStringName, "str");
		String textValue = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam consectetur auctor lectus sed tincidunt. Fusce sodales quis mauris non aliquam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Integer maximus imperdiet velit quis vehicula. Mauris pulvinar amet.";
		entity.set(attrTextName, textValue);
		entity.set(attrXrefName, refEntity0);

		// optional
		entity.set(attrBoolOptionalName, null);
		entity.set(attrCategoricalOptionalName, null);
		entity.set(attrCategoricalMrefOptionalName, null);
		entity.set(attrCompoundAttr0OptionalName, null);
		entity.set(attrCompoundAttrCompoundAttr0OptionalName, null);
		entity.set(attrDateOptionalName, null);
		entity.set(attrDateTimeOptionalName, null);
		entity.set(attrDecimalOptionalName, null);
		entity.set(attrEmailOptionalName, null);
		entity.set(attrEnumOptionalName, null);
		entity.set(attrHtmlOptionalName, null);
		entity.set(attrHyperlinkOptionalName, null);
		entity.set(attrIntOptionalName, null);
		entity.set(attrLongOptionalName, null);
		entity.set(attrMrefOptionalName, null);
		entity.set(attrScriptOptionalName, null);
		entity.set(attrStringOptionalName, null);
		entity.set(attrTextOptionalName, null);
		entity.set(attrXrefOptionalName, null);

		Query<Entity> q = new QueryImpl<>().offset(0).pageSize(100);
		when(dataService.findOneById(ENTITY_NAME, ENTITY_ID)).thenReturn(entity);
		when(dataService.findOneById(eq(ENTITY_NAME), eq(ENTITY_ID), any(Fetch.class))).thenReturn(entity);
		when(dataService.findOneById(eq(SELF_REF_ENTITY_NAME), eq("0"), any(Fetch.class))).thenReturn(selfRefEntity);
		when(dataService.count(ENTITY_NAME, new QueryImpl<>())).thenReturn(2L);
		when(dataService.findAll(ENTITY_NAME, q)).thenReturn(Stream.of(entity));

		when(dataService.findAll(REF_ENTITY_NAME)).thenAnswer(invocation -> Stream.of(refEntity0, refEntity1));

		when(dataService.findOneById(REF_ENTITY_NAME, REF_ENTITY0_ID)).thenReturn(refEntity0);
		when(dataService.findOneById(REF_ENTITY_NAME, REF_ENTITY1_ID)).thenReturn(refEntity1);
		when(dataService.findOneById(REF_REF_ENTITY_NAME, REF_REF_ENTITY_ID)).thenReturn(refRefEntity);
		when(dataService.getEntityType(ENTITY_NAME)).thenReturn(entityType);
		when(dataService.getEntityType(REF_ENTITY_NAME)).thenReturn(refEntityType);
		when(dataService.getEntityType(REF_REF_ENTITY_NAME)).thenReturn(refRefEntityType);
		when(dataService.getEntityType(SELF_REF_ENTITY_NAME)).thenReturn(selfRefEntityType);

		assertEquals(entity.getIdValue(), ENTITY_ID);
		assertEquals(refEntity0.getIdValue(), REF_ENTITY0_ID);
		assertEquals(refEntity0.getLabelValue(), REF_ENTITY0_LABEL);
		assertEquals(refEntity1.getIdValue(), REF_ENTITY1_ID);
		assertEquals(refEntity1.getLabelValue(), REF_ENTITY1_LABEL);
		assertEquals(refRefEntity.getIdValue(), REF_REF_ENTITY_ID);
		assertEquals(selfRefEntity.getIdValue(), "0");

		when(entityManager.create(entityType, POPULATE)).thenAnswer(invocation -> new DynamicEntity(entityType));

		when(localeResolver.resolveLocale(any())).thenReturn(Locale.ENGLISH);

		mockMvc = MockMvcBuilders.standaloneSetup(restControllerV2)
								 .setLocaleResolver(localeResolver)
								 .setMessageConverters(gsonHttpMessageConverter)
								 .setControllerAdvice(new GlobalControllerExceptionHandler(),
										 new SpringExceptionHandler(), new FallbackExceptionHandler())
								 .setConversionService(conversionService)
								 .build();
	}

	private Attribute createAttributeMeta(EntityType entityType, String attrName, AttributeType type)
	{
		return createAttributeMeta(entityType, attrName, type, null);
	}

	private Attribute createAttributeMeta(EntityType entityType, String attrName, AttributeType type,
			EntityType refEntityMeta)
	{
		Attribute attr = attributeFactory.create()
										 .setName(attrName)
										 .setLabel(attrName)
										 .setDataType(type)
										 .setRefEntity(refEntityMeta)
										 .setNillable(true);
		entityType.addAttribute(attr);
		return attr;
	}

	@Test
	public void retrieveAtrributeMetaData()
	{
		assertEquals(restControllerV2.retrieveEntityAttributeMeta(ENTITY_NAME, "id").getHref(),
				"/api/v2/entity/meta/id");
		assertEquals(restControllerV2.retrieveEntityAttributeMeta(ENTITY_NAME, "id").getName(), "id");
		assertEquals(restControllerV2.retrieveEntityAttributeMeta(ENTITY_NAME, "id").getDescription(), null);
	}

	@Test
	public void retrieveAtrributeMetaDataPost()
	{
		assertEquals(restControllerV2.retrieveEntityAttributeMetaPost(ENTITY_NAME, "id").getHref(),
				"/api/v2/entity/meta/id");
		assertEquals(restControllerV2.retrieveEntityAttributeMetaPost(ENTITY_NAME, "id").getName(), "id");
		assertEquals(restControllerV2.retrieveEntityAttributeMetaPost(ENTITY_NAME, "id").getDescription(), null);
	}

	@Test
	public void retrieveResource() throws Exception
	{
		String expectedContent = readFile(getClass().getResourceAsStream("resourceResponse.json"));
		mockMvc.perform(get(HREF_ENTITY_ID))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(expectedContent));
	}

	@Test
	public void retrieveResourceIncludingCategories() throws Exception
	{
		String expectedContent = readFile(getClass().getResourceAsStream("resourceResponseIncludingCategories.json"));
		mockMvc.perform(get(HREF_ENTITY_ID_INCLUDE_CATEGORIES_IS_TRUE))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(expectedContent));
	}

	@Test
	public void retrieveResourcePartialResponseAttribute() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs", attrBoolName))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(
					   readFile(getClass().getResourceAsStream("resourcePartialAttributeResponse.json"))));
	}

	@Test
	public void retrieveResourcePartialResponseAttributeInCompound() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs", attrCompoundName + '(' + attrCompoundAttr0Name + ')'))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(
					   readFile(getClass().getResourceAsStream("resourcePartialAttributeInCompoundResponse.json"))));
	}

	@Test
	public void retrieveResourcePartialResponseAttributeInCompoundInCompound() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs",
				attrCompoundName + '(' + attrCompoundAttrCompoundName + '(' + attrCompoundAttrCompoundAttr0Name + "))"))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(readFile(
					   getClass().getResourceAsStream("resourcePartialAttributeInCompoundInCompoundResponse.json"))));
	}

	@Test
	public void retrieveResourcePartialResponseAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs", attrBoolName + ',' + attrStringName))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(
					   readFile(getClass().getResourceAsStream("resourcePartialAttributesResponse.json"))));
	}

	@Test
	public void retrieveResourcePartialResponseSubAttribute() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs", attrXrefName + '(' + REF_ATTR_VALUE_NAME + ')'))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(
					   readFile(getClass().getResourceAsStream("resourcePartialSubAttributeResponse.json"))));
	}

	@Test
	public void retrieveResourcePartialResponseSubAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs",
				attrXrefName + '(' + REF_ATTR_ID_NAME + ',' + REF_ATTR_VALUE_NAME + ')'))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(
					   readFile(getClass().getResourceAsStream("resourcePartialSubAttributesResponse.json"))));
	}

	@Test
	public void retrieveResourcePartialResponseSubSubAttributes() throws Exception
	{

		String expectedContent = readFile(
				getClass().getResourceAsStream("resourcePartialSubSubAttributesResponse.json"));
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs",
				attrXrefName + '(' + REF_ATTR_ID_NAME + ',' + REF_ATTR_REF_NAME + '(' + REF_REF_ATTR_VALUE_NAME + ')'
						+ ')'))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(expectedContent));
	}

	@Test
	public void retrieveResourceCollection() throws Exception
	{
		String expectedContent = readFile(getClass().getResourceAsStream("resourceCollectionResponse.json"));
		mockMvc.perform(get(HREF_ENTITY_COLLECTION_INCLUDE_CATEGORIES_IS_TRUE))
			   .andExpect(status().isOk())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().json(expectedContent));
	}

	@Test
	public void retrieveEntityCollectionWithZeroNumSize() throws Exception
	{
		// have count return a non null value irrespective of query
		Long countResult = 2L;
		when(dataService.count(anyString(), any())).thenReturn(countResult);
		mockMvc.perform(get(HREF_ENTITY_COLLECTION).param("num", "0"))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$.items").isEmpty())
			   .andExpect(jsonPath("$.total").value(countResult));
	}

	@Test
	public void retrieveEntityCollectionWitNonZeroNumSize() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_COLLECTION))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$.items").isNotEmpty())
			   .andExpect(jsonPath("$.total").value(2L));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateEntities() throws Exception
	{
		mockMvc.perform(
				post(HREF_ENTITY_COLLECTION).content("{entities:[{id:'p1', name:'Piet'}, {id:'p2', name:'Pietje'}]}")
											.contentType(APPLICATION_JSON))
			   .andExpect(status().isCreated())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(jsonPath("$.location", is("/api/v2/entity?q=id=in=(\"p1\",\"p2\")")))
			   .andExpect(jsonPath("$.resources", hasSize(2)))
			   .andExpect(jsonPath("$.resources[0].href", is("/api/v2/entity/p1")))
			   .andExpect(jsonPath("$.resources[1].href", is("/api/v2/entity/p2")));

		verify(dataService).add(eq(ENTITY_NAME), (Stream<Entity>) any(Stream.class));
	}

	@Test
	public void testCreateEntitiesAttribute() throws Exception
	{
		MetaDataService metadataService = mock(MetaDataService.class);
		when(dataService.getMeta()).thenReturn(metadataService);
		when(dataService.getEntityType(ATTRIBUTE_META_DATA)).thenReturn(entityType);
		Attribute attribute0 = mock(Attribute.class);
		when(attribute0.getIdValue()).thenReturn("p1");
		when(attribute0.getEntityType()).thenReturn(entityType);
		Attribute attribute1 = mock(Attribute.class);
		when(attribute1.getIdValue()).thenReturn("p2");
		when(attribute1.getEntityType()).thenReturn(entityType);
		when(entityManager.create(entityType, POPULATE)).thenReturn(attribute0).thenReturn(attribute1);

		mockMvc.perform(post(BASE_URI + '/' + ATTRIBUTE_META_DATA).content(
				"{entities:[{id:'p1', name:'Piet'}, {id:'p2', name:'Pietje'}]}").contentType(APPLICATION_JSON))
			   .andExpect(status().isCreated())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(jsonPath("$.location", is("/api/v2/sys_md_Attribute?q=id=in=(\"p1\",\"p2\")")))
			   .andExpect(jsonPath("$.resources", hasSize(2)))
			   .andExpect(jsonPath("$.resources[0].href", is("/api/v2/sys_md_Attribute/p1")))
			   .andExpect(jsonPath("$.resources[1].href", is("/api/v2/sys_md_Attribute/p2")));

		verify(metadataService).addAttribute(attribute0);
		verify(metadataService).addAttribute(attribute1);
		verifyNoMoreInteractions(metadataService);
	}

	@Test
	public void testCopyEntity() throws Exception
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> repositoryToCopy = mock(Repository.class);
		Package pack = mocksForCopyEntitySuccess(repositoryToCopy);

		String content = "{newEntityName: 'newEntity'}";
		mockMvc.perform(post(HREF_COPY_ENTITY).content(content).contentType(APPLICATION_JSON))
			   .andExpect(status().isCreated())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(content().string("\"org_molgenis_blah_newEntity\""))
			   .andExpect(header().string("Location", "/api/v2/org_molgenis_blah_newEntity"));

		verify(repoCopier).copyRepository(repositoryToCopy, "newEntity", pack, "newEntity");
	}

	@Test
	public void testCopyEntityUnknownEntity() throws Exception
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> repositoryToCopy = mock(Repository.class);
		mocksForCopyEntitySuccess(repositoryToCopy);

		mockMvc.perform(
				post("/api/v2/copy/unknown").content("{newEntityName: 'newEntity'}").contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("Operation failed. Unknown entity: 'unknown'")));
		verifyZeroInteractions(repoCopier);
	}

	@Test
	public void testCopyEntityDuplicateEntity() throws Exception
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> repositoryToCopy = mock(Repository.class);
		mocksForCopyEntitySuccess(repositoryToCopy);

		String content = "{newEntityName: 'duplicateEntity'}";
		mockMvc.perform(post(HREF_COPY_ENTITY).content(content).contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE,
					   is("Operation failed. Duplicate entity: 'org_molgenis_blah_duplicateEntity'")));
		verifyZeroInteractions(repoCopier);
	}

	@Test
	public void testCopyEntityNoReadPermissions() throws Exception
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> repositoryToCopy = mock(Repository.class);
		mocksForCopyEntitySuccess(repositoryToCopy);

		// Override mock
		when(permissionService.hasPermission(new EntityTypeIdentity("entity"), EntityTypePermission.READ)).thenReturn(
				false);

		String content = "{newEntityName: 'newEntity'}";
		mockMvc.perform(post(HREF_COPY_ENTITY).content(content).contentType(APPLICATION_JSON))
			   .andExpect(status().isUnauthorized())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(status().isUnauthorized())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("No read permission on entity entity")));
		verifyZeroInteractions(repoCopier);
	}

	@Test
	public void testCopyEntityNoWriteCapabilities() throws Exception
	{
		@SuppressWarnings("unchecked")
		Repository<Entity> repositoryToCopy = mock(Repository.class);
		mocksForCopyEntitySuccess(repositoryToCopy);

		// Override mock
		Set<RepositoryCapability> capabilities = Sets.newHashSet(RepositoryCapability.AGGREGATEABLE,
				RepositoryCapability.INDEXABLE, RepositoryCapability.QUERYABLE, RepositoryCapability.MANAGABLE);
		when(dataService.getCapabilities("entity")).thenReturn(capabilities);

		mockMvc.perform(post(HREF_COPY_ENTITY).content("{newEntityName: 'newEntity'}").contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("No write capabilities for entity entity")));
		verifyZeroInteractions(repoCopier);
	}

	private Package mocksForCopyEntitySuccess(Repository<Entity> repositoryToCopy)
	{
		Package pack = mock(Package.class);
		when(pack.getId()).thenReturn("org_molgenis_blah");

		when(dataService.hasRepository("entity")).thenReturn(true);
		when(dataService.hasRepository("org_molgenis_blah_duplicateEntity")).thenReturn(true);
		when(dataService.hasRepository("org_molgenis_blah_newEntity")).thenReturn(false);
		when(dataService.getRepository("entity")).thenReturn(repositoryToCopy);

		EntityType entityType = mock(EntityType.class);
		when(entityType.getId()).thenReturn("entityTypeId");
		when(repositoryToCopy.getEntityType()).thenReturn(entityType);
		when(entityType.getPackage()).thenReturn(pack);

		when(repositoryToCopy.getName()).thenReturn("entity");
		when(permissionService.hasPermission(new EntityTypeIdentity("entity"), EntityTypePermission.READ)).thenReturn(
				true);
		Set<RepositoryCapability> capabilities = Sets.newHashSet(RepositoryCapability.WRITABLE);
		when(dataService.getCapabilities("entity")).thenReturn(capabilities);

		@SuppressWarnings("unchecked")
		Repository<Entity> repository = mock(Repository.class);
		when(repository.getName()).thenReturn("org_molgenis_blah_newEntity");
		when(dataService.getRepository("org_molgenis_blah_newEntity")).thenReturn(repository);
		when(repoCopier.copyRepository(repositoryToCopy, "newEntity", pack, "newEntity")).thenReturn(repository);

		doNothing().when(permissionSystemService).giveUserWriteMetaPermissions(any(EntityType.class));
		return pack;
	}

	@Test
	public void testCreateEntitiesNoneProvided() throws Exception
	{
		mockMvc.perform(post(BASE_URI + "/" + "entity").content("{entities:[]}").contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE,
					   is("Please provide at least one entity in the entities property.")));
	}

	@Test
	public void testCreateEntitiesMaxPlusOne() throws Exception
	{
		mockMvc.perform(post(BASE_URI + "/" + "entity").content(createMaxPlusOneEntitiesAsTestContent())
													   .contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("Number of entities cannot be more than 1000.")));
	}

	@Test
	public void testCreateEntitiesUnknownEntityTypeException() throws Exception
	{
		mockMvc.perform(
				post(BASE_URI + "/" + "entity2").content("{entities:[{email:'test@email.com', extraAttribute:'test'}]}")
												.contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("Operation failed. Unknown entity: 'entity2'")));
	}

	/**
	 * createMolgenisDataExceptionUnknownIdentifier
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateEntitiesSystemException() throws Exception
	{
		Exception e = new MolgenisDataException("Check if this exception is not swallowed by the system");
		doThrow(e).when(dataService).add(eq(ENTITY_NAME), (Stream<Entity>) any(Stream.class));

		String content = "{entities:[{id:'p1', name:'Example data'}]}";
		ResultActions resultActions = mockMvc.perform(
				post(HREF_ENTITY_COLLECTION).content(content).contentType(APPLICATION_JSON))
											 .andExpect(status().isBadRequest())
											 .andExpect(content().contentType(APPLICATION_JSON_UTF8))
											 .andExpect(header().doesNotExist("Location"))
											 .andExpect(jsonPath(FIRST_ERROR_MESSAGE,
													 is("Check if this exception is not swallowed by the system")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateEntities() throws Exception
	{
		String content = "{entities:[{id:'p1', name:'Witte Piet'}, {id:'p2', name:'Zwarte Piet'}]}";
		mockMvc.perform(put(HREF_ENTITY_COLLECTION).content(content).contentType(APPLICATION_JSON))
			   .andExpect(status().isOk());

		verify(dataService, times(1)).update(eq(ENTITY_NAME), (Stream<Entity>) any(Stream.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateEntitiesMolgenisDataException() throws Exception
	{
		Exception e = new MolgenisDataException("Check if this exception is not swallowed by the system");
		doThrow(e).when(dataService).update(eq(ENTITY_NAME), (Stream<Entity>) any(Stream.class));

		mockMvc.perform(put(HREF_ENTITY_COLLECTION).content("{entities:[{id:'p1', name:'Example data'}]}")
												   .contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(header().doesNotExist("Location"))
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("Check if this exception is not swallowed by the system")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateEntitiesMolgenisValidationException() throws Exception
	{
		Exception e = new MolgenisValidationException(Collections.singleton(new ConstraintViolation("Message", 5L)));
		doThrow(e).when(dataService).update(eq(ENTITY_NAME), (Stream<Entity>) any(Stream.class));

		mockMvc.perform(put(HREF_ENTITY_COLLECTION).content("{entities:[{id:'p1', name:'Example data'}]}")
												   .contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(content().contentType(APPLICATION_JSON_UTF8))
			   .andExpect(header().doesNotExist("Location"))
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("Message (entity 5)")));
	}

	@Test
	public void testUpdateEntitiesNoEntities() throws Exception
	{
		mockMvc.perform(put(BASE_URI + "/" + "entity").content("{entities:[]}").contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE,
					   is("Please provide at least one entity in the entities property.")));
	}

	@Test
	public void testUpdateEntitiesMaxEntitiesExceeded() throws Exception
	{
		mockMvc.perform(put(BASE_URI + "/" + "entity").content(this.createMaxPlusOneEntitiesAsTestContent())
													  .contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("Number of entities cannot be more than 1000.")));
	}

	@Test
	public void testUpdateEntitiesUnknownEntity() throws Exception
	{
		mockMvc.perform(put(BASE_URI + "/" + "entity2").content("{entities:[{email:'test@email.com'}]}")
													   .contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("Operation failed. Unknown entity: 'entity2'")));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateEntitiesSpecificAttribute() throws Exception
	{
		mockMvc.perform(put(HREF_ENTITY_COLLECTION + "/date_time").content(
				"{entities:[{id:'0', date_time:'1985-08-12T08:12:13+0200'}]}").contentType(APPLICATION_JSON))
			   .andExpect(status().isOk());

		verify(dataService, times(1)).update(eq(ENTITY_NAME), (Stream<Entity>) any(Stream.class));

		Entity entity = dataService.findOneById(ENTITY_NAME, ENTITY_ID);
		assertEquals(entity.get("date_time"), MolgenisDateFormat.parseInstant("1985-08-12T08:12:13+0200"));
	}

	@Test
	public void testUpdateEntitiesSpecificAttributeNoExceptions() throws Exception
	{
		mockMvc.perform(
				put(BASE_URI + "/" + "entity" + "/" + "email").content("{entities:[]}").contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE,
					   is("Please provide at least one entity in the entities property.")));
	}

	@Test
	public void testUpdateEntitiesSpecificAttributeMaxEntitiesExceeded() throws Exception
	{
		mockMvc.perform(
				put(BASE_URI + "/" + "entity" + "/" + "email").content(this.createMaxPlusOneEntitiesAsTestContent())
															  .contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("Number of entities cannot be more than 1000.")));
	}

	@Test
	public void testUpdateEntitiesSpecificAttributeUnknownEntity() throws Exception
	{
		mockMvc.perform(put(BASE_URI + "/" + "entity2" + "/" + "email").content("{entities:[{email:'test@email.com'}]}")
																	   .contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("Operation failed. Unknown entity: 'entity2'")));
	}

	@Test
	public void testUpdateEntitiesSpecificAttributeReadOnlyAttribute() throws Exception
	{
		ResultActions resultActions = mockMvc.perform(
				put(BASE_URI + "/" + "entity" + "/" + "decimal").content("{entities:[{decimal:'42'}]}")
																.contentType(APPLICATION_JSON))
											 .andExpect(jsonPath(FIRST_ERROR_MESSAGE,
													 is("Operation failed. Attribute 'decimal' of entity 'entity' is readonly")));
	}

	@DataProvider(name = "testDeleteEntityCollection")
	public static Iterator<Object[]> testDeleteEntityCollectionProvider() throws ParseException
	{
		return Arrays.asList(new Object[] { STRING, asList("0", "1") }, new Object[] { INT, asList(0, 1) },
				new Object[] { LONG, asList(0L, 1L) }, new Object[] { EMAIL, asList("0", "1") },
				new Object[] { HYPERLINK, asList("0", "1") }).iterator();
	}

	@Test(dataProvider = "testDeleteEntityCollection")
	public void testDeleteEntityCollection(AttributeType idAttrType, List<Object> expectedIds) throws Exception
	{
		EntityType entityType = mock(EntityType.class);
		Attribute idAttr = when(mock(Attribute.class).getDataType()).thenReturn(idAttrType).getMock();
		when(entityType.getIdAttribute()).thenReturn(idAttr);
		when(dataService.getEntityType("MyEntityType")).thenReturn(entityType);
		this.mockMvc.perform(
				delete("/api/v2/MyEntityType").contentType(APPLICATION_JSON).content("{\"entityIds\":[\"0\",\"1\"]}"))
					.andExpect(status().isNoContent());

		@SuppressWarnings("unchecked")
		ArgumentCaptor<Stream<Object>> captor = ArgumentCaptor.forClass(Stream.class);
		verify(dataService).deleteAll(eq("MyEntityType"), captor.capture());
		assertEquals(captor.getValue().collect(toList()), expectedIds);
	}

	@Test
	public void testDeleteEntityCollectionExceptionAbstractEntity() throws Exception
	{
		EntityType entityType = when(mock(EntityType.class).isAbstract()).thenReturn(true).getMock();
		when(dataService.getEntityType("MyEntityType")).thenReturn(entityType);
		this.mockMvc.perform(
				delete("/api/v2/MyEntityType").contentType(APPLICATION_JSON).content("{\"entityIds\":[\"id0\"]}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath(FIRST_ERROR_MESSAGE,
							is("Cannot delete entities because type [MyEntityType] is abstract.")));
	}

	@Test
	public void testDeleteEntityCollectionExceptionUnknownEntity() throws Exception
	{
		when(dataService.getEntityType("MyEntityType")).thenThrow(
				new UnknownEntityException("Unknown entity [MyEntityType]"));

		mockMvc.perform(
				delete("/api/v2/MyEntityType").contentType(APPLICATION_JSON).content("{\"entityIds\":[\"id0\"]}"))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("Unknown entity [MyEntityType]")));
	}

	@Test
	public void testDeleteEntityCollectionExceptionNoEntitiesToDelete() throws Exception
	{
		mockMvc.perform(delete("/api/v2/MyEntityType").contentType(APPLICATION_JSON).content("{\"entityIds\":[]}"))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE,
					   is("Please provide at least one entity in the entityIds property.")));
	}

	@Test
	public void testDeleteEntityCollectionExceptionInvalidRequestBody() throws Exception
	{
		this.mockMvc.perform(delete("/api/v2/MyEntityType").contentType(APPLICATION_JSON).content("invalid"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("Invalid request body.")));
	}

	@Test
	public void testSelfRefWithAllAttrsEqualsSelfRefWithoutAttrs() throws Exception
	{
		String withAttrs = mockMvc.perform(get(BASE_URI + "/selfRefEntity/0?attrs=*").contentType(APPLICATION_JSON))
								  .andExpect(status().isOk())
								  .andExpect(jsonPath("$.selfRef._href", is("/api/v2/selfRefEntity/0")))
								  .andExpect(jsonPath("$.selfRef.id", is("0")))
								  .andReturn()
								  .getResponse()
								  .getContentAsString();
		String withoutAttrs = mockMvc.perform(get(BASE_URI + "/selfRefEntity/0").contentType(APPLICATION_JSON))
									 .andExpect(status().isOk())
									 .andReturn()
									 .getResponse()
									 .getContentAsString();
		assertEquals(withAttrs, withoutAttrs);
	}

	@Test
	public void testSelfRefWithNestedFetch() throws Exception
	{
		mockMvc.perform(get(BASE_URI + "/selfRefEntity/0?attrs=*,selfRef(*,selfRef(*))").contentType(APPLICATION_JSON))
			   .andExpect(status().isOk())
			   .andExpect(jsonPath("$.selfRef.selfRef._href", is("/api/v2/selfRefEntity/0")))
			   .andExpect(jsonPath("$.selfRef.selfRef.id", is("0")))
			   .andExpect(jsonPath("$.selfRef.selfRef.selfRef._href", is("/api/v2/selfRefEntity/0")))
			   .andExpect(jsonPath("$.selfRef.selfRef.selfRef.id", is("0")));
	}

	/**
	 * createMolgenisDataExceptionIdentifierAndValue
	 */
	@Test
	public void testUpdateEntitiesMustProvideIdentifierAndValue() throws Exception
	{
		mockMvc.perform(put(BASE_URI + "/" + "entity" + "/" + "email").content(
				"{entities:[{id:0,email:'test@email.com',extraAttribute:'test'}]}").contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE,
					   is("Operation failed. Entities must provide only an identifier and a value")));
	}

	@Test
	public void testUpdateEntitiesSpecificAttributeUnknownIdentifier() throws Exception
	{
		mockMvc.perform(put(BASE_URI + "/" + "entity" + "/" + "email").content(
				"{entities:[{email:'test@email.com', extraAttribute:'test'}]}").contentType(APPLICATION_JSON))
			   .andExpect(status().isBadRequest())
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("Operation failed. Unknown identifier on index 0")));
	}

	@Test
	public void testUpdateEntitiesSpecificAttributeInvalidId() throws Exception
	{
		mockMvc.perform(
				put(BASE_URI + "/entity/email").content("{\"entities\":[{\"id\":\"4\",\"email\":\"test@email.com\"}]}")
											   .contentType(APPLICATION_JSON))
			   .andExpect(jsonPath(FIRST_ERROR_MESSAGE, is("The entity you are trying to update [4] does not exist.")));
	}

	private String createMaxPlusOneEntitiesAsTestContent()
	{
		StringBuilder c = new StringBuilder();
		c.append("{entities:[");
		IntStream.range(0, RestControllerV2.MAX_ENTITIES).forEach(i -> c.append("{email:'test@email.com'},"));
		c.append("{email:'test@email.com'}]}");
		return c.toString();
	}

	@Configuration
	public static class RestControllerV2Config extends WebMvcConfigurerAdapter
	{
		@Bean
		public FormattingConversionService conversionService()
		{
			FormattingConversionServiceFactoryBean conversionServiceFactoryBean = new FormattingConversionServiceFactoryBean();
			conversionServiceFactoryBean.setConverters(Collections.singleton(new AttributeFilterConverter()));
			conversionServiceFactoryBean.afterPropertiesSet();
			return conversionServiceFactoryBean.getObject();
		}

		@Bean
		public static PropertySourcesPlaceholderConfigurer properties() throws Exception
		{
			final PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
			Properties properties = new Properties();
			properties.setProperty("environment", "test");
			pspc.setProperties(properties);
			return pspc;
		}

		@Bean
		public DataService dataService()
		{
			return mock(DataService.class);
		}

		@Bean
		public UserPermissionEvaluator permissionService()
		{
			return mock(UserPermissionEvaluator.class);
		}

		@Bean
		public PermissionSystemService permissionSystemService()
		{
			return mock(PermissionSystemService.class);
		}

		@Bean
		public RepositoryCopier repositoryCopier()
		{
			return mock(RepositoryCopier.class);
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
		public LocaleResolver localeResolver()
		{
			return mock(LocaleResolver.class);
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
		public LocalizationService localizationService()
		{
			return mock(LocalizationService.class);
		}

		@Bean
		public ServletUriComponentsBuilderFactory servletUriComponentsBuilderFactory()
		{
			return mock(ServletUriComponentsBuilderFactory.class);
		}

		@Bean
		public RestControllerV2 restController()
		{
			return new RestControllerV2(dataService(), permissionService(),
					new RestService(dataService(), idGenerator(), fileStore(), fileMetaFactory(), entityManager(),
							servletUriComponentsBuilderFactory()), localizationService(), permissionSystemService(),
					repositoryCopier());
		}
	}
}

