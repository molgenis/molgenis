package org.molgenis.data.rest.v2;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.MolgenisFieldTypes.AttributeType;
import org.molgenis.data.*;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.model.*;
import org.molgenis.data.meta.model.Package;
import org.molgenis.data.rest.service.RestService;
import org.molgenis.data.rest.v2.RestControllerV2Test.RestControllerV2Config;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.file.FileStore;
import org.molgenis.file.model.FileMetaFactory;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.security.permission.PermissionSystemService;
import org.molgenis.test.data.AbstractMolgenisSpringTest;
import org.molgenis.util.GsonConfig;
import org.molgenis.util.GsonHttpMessageConverter;
import org.molgenis.util.MolgenisDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.molgenis.MolgenisFieldTypes.AttributeType.*;
import static org.molgenis.data.meta.model.EntityMetaData.AttributeRole.*;
import static org.molgenis.util.MolgenisDateFormat.getDateFormat;
import static org.molgenis.util.MolgenisDateFormat.getDateTimeFormat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.testng.Assert.assertEquals;

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
	private static final String REF_ENTITY1_ID = "ref0";
	private static final String REF_REF_ENTITY_ID = "refRef0";
	private static final String HREF_ENTITY_COLLECTION = RestControllerV2.BASE_URI + '/' + ENTITY_NAME;
	private static final String HREF_COPY_ENTITY = RestControllerV2.BASE_URI + "/copy/" + ENTITY_NAME;
	private static final String HREF_ENTITY_ID = HREF_ENTITY_COLLECTION + '/' + ENTITY_ID;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	private RestControllerV2 restControllerV2;

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private FormattingConversionService conversionService;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@Autowired
	private MolgenisPermissionService molgenisPermissionService;

	@Autowired
	private PermissionSystemService permissionSystemService;

	@Autowired
	private Gson gson;

	@Autowired
	private DataService dataService;

	private MockMvc mockMvc;
	private String attrBoolName;
	private String attrStringName;
	private String attrXrefName;
	private String attrCompoundName;
	private String attrCompoundAttr0Name;
	private String attrCompoundAttrCompoundName;
	private String attrCompoundAttrCompoundAttr0Name;

	@BeforeMethod
	public void beforeMethod() throws ParseException
	{
		reset(dataService);

		EntityMetaData refRefEntityMeta = entityMetaDataFactory.create().setName(REF_REF_ENTITY_NAME)
				.setLabel(REF_REF_ENTITY_NAME)
				.addAttribute(attributeMetaDataFactory.create().setName(REF_REF_ATTR_ID_NAME), ROLE_ID, ROLE_LABEL,
						ROLE_LOOKUP).addAttribute(attributeMetaDataFactory.create().setName(REF_REF_ATTR_VALUE_NAME));

		EntityMetaData selfRefEntityMeta = entityMetaDataFactory.create().setName(SELF_REF_ENTITY_NAME)
				.setLabel(SELF_REF_ENTITY_NAME)
				.addAttribute(attributeMetaDataFactory.create().setName("id"), ROLE_ID, ROLE_LABEL, ROLE_LOOKUP);
		selfRefEntityMeta.addAttribute(
				attributeMetaDataFactory.create().setName("selfRef").setDataType(XREF).setRefEntity(selfRefEntityMeta));

		Entity selfRefEntity = new DynamicEntity(selfRefEntityMeta);
		selfRefEntity.set("id", "0");
		selfRefEntity.set("selfRef", selfRefEntity);

		EntityMetaData refEntityMeta = entityMetaDataFactory.create().setName(REF_ENTITY_NAME).setLabel(REF_ENTITY_NAME)
				.addAttribute(attributeMetaDataFactory.create().setName(REF_ATTR_ID_NAME), ROLE_ID, ROLE_LABEL,
						ROLE_LOOKUP).addAttribute(attributeMetaDataFactory.create().setName(REF_ATTR_VALUE_NAME))
				.addAttribute(attributeMetaDataFactory.create().setName(REF_ATTR_REF_NAME).setDataType(XREF)
						.setRefEntity(refRefEntityMeta));
		// required
		String attrIdName = "id";
		attrBoolName = "bool";
		String attrCategoricalName = "categorical";
		String attrCategoricalMrefName = "categorical_mref";
		attrCompoundName = "compound";
		attrCompoundAttr0Name = "compound_attr0";
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
		EntityMetaData entityMeta = entityMetaDataFactory.create().setName(ENTITY_NAME).setLabel(ENTITY_NAME);
		AttributeMetaData attrId = attributeMetaDataFactory.create().setName(attrIdName);
		entityMeta.addAttribute(attrId, ROLE_ID, ROLE_LABEL, ROLE_LOOKUP);
		AttributeMetaData attrBool = createAttributeMeta(entityMeta, attrBoolName, BOOL).setNillable(false);
		AttributeMetaData attrCategorical = createAttributeMeta(entityMeta, attrCategoricalName, CATEGORICAL,
				refEntityMeta).setNillable(false);
		AttributeMetaData attrCategoricalMref = createAttributeMeta(entityMeta, attrCategoricalMrefName,
				CATEGORICAL_MREF, refEntityMeta).setNillable(false);
		AttributeMetaData attrCompound = createAttributeMeta(entityMeta, attrCompoundName, COMPOUND);
		AttributeMetaData attrDate = createAttributeMeta(entityMeta, attrDateName, DATE).setNillable(false);
		AttributeMetaData attrDateTime = createAttributeMeta(entityMeta, attrDateTimeName, DATE_TIME)
				.setNillable(false);
		AttributeMetaData attrDecimal = createAttributeMeta(entityMeta, attrDecimalName, DECIMAL, null)
				.setReadOnly(true).setNillable(false);
		AttributeMetaData attrEmail = createAttributeMeta(entityMeta, attrEmailName, EMAIL).setNillable(false);
		AttributeMetaData attrEnum = createAttributeMeta(entityMeta, attrEnumName, ENUM)
				.setEnumOptions(asList(enum0, enum1, enum2)).setNillable(false);
		AttributeMetaData attrHtml = createAttributeMeta(entityMeta, attrHtmlName, HTML).setNillable(false);
		AttributeMetaData attrHyperlink = createAttributeMeta(entityMeta, attrHyperlinkName, HYPERLINK)
				.setNillable(false);
		AttributeMetaData attrInt = createAttributeMeta(entityMeta, attrIntName, INT).setNillable(false);
		AttributeMetaData attrLong = createAttributeMeta(entityMeta, attrLongName, LONG).setNillable(false);
		AttributeMetaData attrMref = createAttributeMeta(entityMeta, attrMrefName, MREF, refEntityMeta)
				.setNillable(false);
		AttributeMetaData attrScript = createAttributeMeta(entityMeta, attrScriptName, SCRIPT).setNillable(false);
		AttributeMetaData attrString = createAttributeMeta(entityMeta, attrStringName, STRING).setNillable(false);
		AttributeMetaData attrText = createAttributeMeta(entityMeta, attrTextName, TEXT).setNillable(false);
		AttributeMetaData attrXref = createAttributeMeta(entityMeta, attrXrefName, XREF, refEntityMeta)
				.setNillable(false);

		// optional
		AttributeMetaData attrBoolOptional = createAttributeMeta(entityMeta, attrBoolOptionalName, BOOL);
		AttributeMetaData attrCategoricalOptional = createAttributeMeta(entityMeta, attrCategoricalOptionalName,
				CATEGORICAL, refEntityMeta);
		AttributeMetaData attrCategoricalMrefOptional = createAttributeMeta(entityMeta, attrCategoricalMrefOptionalName,
				CATEGORICAL_MREF, refEntityMeta);
		AttributeMetaData attrDateOptional = createAttributeMeta(entityMeta, attrDateOptionalName, DATE);
		AttributeMetaData attrDateTimeOptional = createAttributeMeta(entityMeta, attrDateTimeOptionalName, DATE_TIME);
		AttributeMetaData attrDecimalOptional = createAttributeMeta(entityMeta, attrDecimalOptionalName, DECIMAL, null);
		AttributeMetaData attrEmailOptional = createAttributeMeta(entityMeta, attrEmailOptionalName, EMAIL);
		AttributeMetaData attrEnumOptional = createAttributeMeta(entityMeta, attrEnumOptionalName, ENUM)
				.setEnumOptions(asList(enum0, enum1, enum2));
		AttributeMetaData attrHtmlOptional = createAttributeMeta(entityMeta, attrHtmlOptionalName, HTML);
		AttributeMetaData attrHyperlinkOptional = createAttributeMeta(entityMeta, attrHyperlinkOptionalName, HYPERLINK);
		AttributeMetaData attrIntOptional = createAttributeMeta(entityMeta, attrIntOptionalName, INT);
		AttributeMetaData attrLongOptional = createAttributeMeta(entityMeta, attrLongOptionalName, LONG);
		AttributeMetaData attrMrefOptional = createAttributeMeta(entityMeta, attrMrefOptionalName, MREF, refEntityMeta);
		AttributeMetaData attrScriptOptional = createAttributeMeta(entityMeta, attrScriptOptionalName, SCRIPT);
		AttributeMetaData attrStringOptional = createAttributeMeta(entityMeta, attrStringOptionalName, STRING);
		AttributeMetaData attrTextOptional = createAttributeMeta(entityMeta, attrTextOptionalName, TEXT);
		AttributeMetaData attrXrefOptional = createAttributeMeta(entityMeta, attrXrefOptionalName, XREF, refEntityMeta);

		AttributeMetaData compoundAttrCompoundAttr0 = createAttributeMeta(null, attrCompoundAttrCompoundAttr0Name,
				STRING).setNillable(false);
		AttributeMetaData compoundAttrCompoundAttr0Optional = createAttributeMeta(null,
				attrCompoundAttrCompoundAttr0OptionalName, STRING).setNillable(true);

		AttributeMetaData compoundAttrCompound = createAttributeMeta(null, attrCompoundAttrCompoundName, COMPOUND);
		compoundAttrCompound.setAttributeParts(asList(compoundAttrCompoundAttr0, compoundAttrCompoundAttr0Optional));

		AttributeMetaData compoundAttr0 = createAttributeMeta(null, attrCompoundAttr0Name, STRING).setNillable(false);
		AttributeMetaData compoundAttr0Optional = createAttributeMeta(null, attrCompoundAttr0OptionalName, STRING)
				.setNillable(true);
		attrCompound.setAttributeParts(asList(compoundAttr0, compoundAttr0Optional, compoundAttrCompound));

		Entity refRefEntity = new DynamicEntity(refRefEntityMeta);
		refRefEntity.set(REF_REF_ATTR_ID_NAME, REF_REF_ENTITY_ID);
		refRefEntity.set(REF_REF_ATTR_VALUE_NAME, "value");

		Entity refEntity0 = new DynamicEntity(refEntityMeta);
		refEntity0.set(REF_ATTR_ID_NAME, REF_ENTITY0_ID);
		refEntity0.set(REF_ATTR_VALUE_NAME, "val0");
		refEntity0.set(REF_ATTR_REF_NAME, refRefEntity);

		Entity refEntity1 = new DynamicEntity(refEntityMeta);
		refEntity1.set(REF_ATTR_ID_NAME, REF_ENTITY1_ID);
		refEntity1.set(REF_ATTR_VALUE_NAME, "val1");
		refEntity1.set(REF_ATTR_REF_NAME, refRefEntity);

		Entity entity = new DynamicEntity(entityMeta);

		// required
		entity.set(attrIdName, ENTITY_ID);
		entity.set(attrBoolName, true);
		entity.set(attrCategoricalName, refEntity0);
		entity.set(attrCategoricalMrefName, asList(refEntity0, refEntity1));
		entity.set(attrCompoundAttr0Name, "compoundAttr0Str");
		entity.set(attrCompoundAttrCompoundAttr0Name, "compoundAttrCompoundAttr0Str");
		entity.set(attrDateName, getDateFormat().parse("2015-05-22"));
		entity.set(attrDateTimeName, getDateTimeFormat().parse("2015-05-22T11:12:13+0500"));
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

		Query<Entity> q = new QueryImpl<Entity>().offset(0).pageSize(100);
		when(dataService.findOneById(ENTITY_NAME, ENTITY_ID)).thenReturn(entity);
		when(dataService.findOneById(eq(ENTITY_NAME), eq(ENTITY_ID), any(Fetch.class))).thenReturn(entity);
		when(dataService.findOneById(eq(SELF_REF_ENTITY_NAME), eq("0"), any(Fetch.class))).thenReturn(selfRefEntity);
		when(dataService.count(ENTITY_NAME, q)).thenReturn(2l);
		when(dataService.findAll(ENTITY_NAME, q)).thenReturn(Stream.of(entity));
		when(dataService.findOneById(REF_ENTITY_NAME, REF_ENTITY0_ID)).thenReturn(refEntity0);
		when(dataService.findOneById(REF_ENTITY_NAME, REF_ENTITY1_ID)).thenReturn(refEntity1);
		when(dataService.findOneById(REF_REF_ENTITY_NAME, REF_REF_ENTITY_ID)).thenReturn(refRefEntity);
		when(dataService.getEntityMetaData(ENTITY_NAME)).thenReturn(entityMeta);
		when(dataService.getEntityMetaData(REF_ENTITY_NAME)).thenReturn(refEntityMeta);
		when(dataService.getEntityMetaData(REF_REF_ENTITY_NAME)).thenReturn(refRefEntityMeta);
		when(dataService.getEntityMetaData(SELF_REF_ENTITY_NAME)).thenReturn(selfRefEntityMeta);

		assertEquals(entity.getIdValue(), ENTITY_ID);
		assertEquals(refEntity0.getIdValue(), REF_ENTITY0_ID);
		assertEquals(refEntity1.getIdValue(), REF_ENTITY1_ID);
		assertEquals(refRefEntity.getIdValue(), REF_REF_ENTITY_ID);
		assertEquals(selfRefEntity.getIdValue(), "0");

		when(entityManager.create(entityMeta)).thenAnswer(new Answer<Entity>()
		{
			@Override
			public Entity answer(InvocationOnMock invocation) throws Throwable
			{
				return new DynamicEntity(entityMeta);
			}
		});

		mockMvc = MockMvcBuilders.standaloneSetup(restControllerV2).setMessageConverters(gsonHttpMessageConverter)
				.setConversionService(conversionService).build();
	}

	private AttributeMetaData createAttributeMeta(EntityMetaData entityMeta, String attrName, AttributeType type)
	{
		return createAttributeMeta(entityMeta, attrName, type, null);
	}

	private AttributeMetaData createAttributeMeta(EntityMetaData entityMeta, String attrName, AttributeType type,
			EntityMetaData refEntityMeta)
	{
		AttributeMetaData attr = attributeMetaDataFactory.create().setName(attrName).setLabel(attrName)
				.setDataType(type).setRefEntity(refEntityMeta).setNillable(true);

		if (entityMeta != null)
		{
			entityMeta.addAttribute(attr);
		}
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
		mockMvc.perform(get(HREF_ENTITY_ID)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON)).andExpect(content().string(resourceResponse));
	}

	@Test
	public void retrieveResourcePartialResponseAttribute() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs", attrBoolName)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(resourcePartialAttributeResponse));
	}

	@Test
	public void retrieveResourcePartialResponseAttributeInCompound() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs", attrCompoundName + '(' + attrCompoundAttr0Name + ')'))
				.andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(resourcePartialAttributeInCompoundResponse));
	}

	@Test
	public void retrieveResourcePartialResponseAttributeInCompoundInCompound() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs",
				attrCompoundName + '(' + attrCompoundAttrCompoundName + '(' + attrCompoundAttrCompoundAttr0Name + "))"))
				.andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(resourcePartialAttributeInCompoundInCompoundResponse));
	}

	@Test
	public void retrieveResourcePartialResponseAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs", attrBoolName + ',' + attrStringName))
				.andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(resourcePartialAttributesResponse));
	}

	@Test
	public void retrieveResourcePartialResponseSubAttribute() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs", attrXrefName + '(' + REF_ATTR_VALUE_NAME + ')'))
				.andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(resourcePartialSubAttributeResponse));
	}

	@Test
	public void retrieveResourcePartialResponseSubAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID)
				.param("attrs", attrXrefName + '(' + REF_ATTR_ID_NAME + ',' + REF_ATTR_VALUE_NAME + ')'))
				.andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(resourcePartialSubAttributesResponse));
	}

	@Test
	public void retrieveResourcePartialResponseSubSubAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs",
				attrXrefName + '(' + REF_ATTR_ID_NAME + ',' + REF_ATTR_REF_NAME + '(' + REF_REF_ATTR_VALUE_NAME + ')'
						+ ')')).andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(resourcePartialSubSubAttributesResponse));
	}

	@Test
	public void retrieveResourceCollection() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_COLLECTION)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(resourceCollectionResponse));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testCreateEntities() throws Exception
	{
		String content = "{entities:[{id:'p1', name:'Piet'}, {id:'p2', name:'Pietje'}]}";
		String responseBody =
				"{\n  \"location\": \"/api/v2/entity?q=id=in=(\\\"p1\\\",\\\"p2\\\")\",\n  \"resources\": [\n    {\n      \"href\": \"/api/v2/entity/p1\"\n    },\n"
						+ "    {\n      \"href\": \"/api/v2/entity/p2\"\n    }\n  ]\n}";
		mockMvc.perform(post(HREF_ENTITY_COLLECTION).content(content).contentType(APPLICATION_JSON))
				.andExpect(status().isCreated()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(responseBody));

		verify(dataService).add(eq(ENTITY_NAME), (Stream<Entity>) any(Stream.class));
	}

	@Test
	public void testCopyEntity() throws Exception
	{
		Repository<Entity> repositoryToCopy = mock(Repository.class);
		mocksForCopyEntitySucces(repositoryToCopy);

		String content = "{newEntityName: 'newEntity'}";
		String responseBody = "\"newEntity\"";
		MockHttpServletRequestBuilder mockHttpServletRequestBuilder = post(HREF_COPY_ENTITY).content(content)
				.contentType(APPLICATION_JSON);
		mockMvc.perform(mockHttpServletRequestBuilder).andExpect(status().isCreated())
				.andExpect(content().contentType(APPLICATION_JSON)).andExpect(content().string(responseBody))
				.andExpect(header().string("Location", "/api/v2/newEntity"));

		verify(dataService).copyRepository(repositoryToCopy, "newEntity", "newEntity");
	}

	@Test
	public void testCopyEntityUnknownEntity() throws Exception
	{
		Repository<Entity> repositoryToCopy = mock(Repository.class);
		mocksForCopyEntitySucces(repositoryToCopy);

		String content = "{newEntityName: 'newEntity'}";
		ResultActions resultActions = mockMvc
				.perform(post("/api/v2/copy/unknown").content(content).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON));

		this.assertEqualsErrorMessage(resultActions, "Operation failed. Unknown entity: 'unknown'");
		verify(dataService, never()).copyRepository(repositoryToCopy, "unknown", "unknown");
	}

	@Test
	public void testCopyEntityDuplicateEntity() throws Exception
	{
		Repository<Entity> repositoryToCopy = mock(Repository.class);
		mocksForCopyEntitySucces(repositoryToCopy);

		String content = "{newEntityName: 'entity'}";
		ResultActions resultActions = mockMvc
				.perform(post(HREF_COPY_ENTITY).content(content).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON));

		this.assertEqualsErrorMessage(resultActions, "Operation failed. Duplicate entity: 'entity'");
		verify(dataService, never()).copyRepository(repositoryToCopy, "entity", "entity");
	}

	@Test
	public void testCopyEntityNoReadPermissions() throws Exception
	{
		Repository<Entity> repositoryToCopy = mock(Repository.class);
		mocksForCopyEntitySucces(repositoryToCopy);

		// Override mock
		when(molgenisPermissionService.hasPermissionOnEntity("entity", Permission.READ)).thenReturn(false);

		String content = "{newEntityName: 'newEntity'}";
		ResultActions resultActions = mockMvc
				.perform(post(HREF_COPY_ENTITY).content(content).contentType(APPLICATION_JSON))
				.andExpect(status().isUnauthorized()).andExpect(content().contentType(APPLICATION_JSON));

		this.assertEqualsErrorMessage(resultActions, "No read permission on entity entity");
		verify(dataService, never()).copyRepository(repositoryToCopy, "newEntity", "newEntity");
	}

	@Test
	public void testCopyEntityNoWriteCapabilities() throws Exception
	{
		Repository<Entity> repositoryToCopy = mock(Repository.class);
		mocksForCopyEntitySucces(repositoryToCopy);

		// Override mock
		Set<RepositoryCapability> capabilities = Sets
				.newHashSet(RepositoryCapability.AGGREGATEABLE, RepositoryCapability.INDEXABLE,
						RepositoryCapability.QUERYABLE, RepositoryCapability.MANAGABLE);
		when(dataService.getCapabilities("entity")).thenReturn(capabilities);

		String content = "{newEntityName: 'newEntity'}";
		ResultActions resultActions = mockMvc
				.perform(post(HREF_COPY_ENTITY).content(content).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON));

		this.assertEqualsErrorMessage(resultActions, "No write capabilities for entity entity");
		verify(dataService, never()).copyRepository(repositoryToCopy, "newEntity", "newEntity");
	}

	private void mocksForCopyEntitySucces(Repository<Entity> repositoryToCopy)
	{
		when(dataService.hasRepository("entity")).thenReturn(true);
		when(dataService.hasRepository("newEntity")).thenReturn(false);
		when(dataService.getRepository("entity")).thenReturn(repositoryToCopy);

		// Return package name
		EntityMetaData entityMetaData = mock(EntityMetaData.class);
		when(repositoryToCopy.getEntityMetaData()).thenReturn(entityMetaData);
		Package package_ = mock(Package.class);
		when(entityMetaData.getPackage()).thenReturn(package_);
		when(package_.getName()).thenReturn("base");

		when(repositoryToCopy.getName()).thenReturn("entity");
		when(molgenisPermissionService.hasPermissionOnEntity("entity", Permission.READ)).thenReturn(true);
		Set<RepositoryCapability> capabilities = Sets.newHashSet(RepositoryCapability.WRITABLE);
		when(dataService.getCapabilities("entity")).thenReturn(capabilities);

		Repository<Entity> repository = mock(Repository.class);
		when(repository.getName()).thenReturn("newEntity");
		when(dataService.getRepository("newEntity")).thenReturn(repository);
		when(dataService.copyRepository(repositoryToCopy, "newEntity", "newEntity")).thenReturn(repository);

		doNothing().when(permissionSystemService)
				.giveUserEntityPermissions(any(SecurityContext.class), Collections.singletonList(any(String.class)));
	}

	/**
	 * EXCEPTION_NO_ENTITIES
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateEntitiesExceptions1() throws Exception
	{
		this.testCreateEntitiesExceptions("entity", "{entities:[]}",
				"Please provide at least one entity in the entities property.");
	}

	/**
	 * EXCEPTION_MAX_ENTITIES_EXCEEDED
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateEntitiesExceptions2() throws Exception
	{
		this.testCreateEntitiesExceptions("entity", this.createMaxPlusOneEntitiesAsTestContent(),
				"Number of entities cannot be more than 1000.");
	}

	/**
	 * createUnknownEntityException
	 *
	 * @throws Exception
	 */
	@Test
	public void testCreateEntitiesExceptions3() throws Exception
	{
		this.testCreateEntitiesExceptions("entity2", "{entities:[{email:'test@email.com', extraAttribute:'test'}]}",
				RestControllerV2.createUnknownEntityException("entity2").getMessage());
	}

	/**
	 * createMolgenisDataExceptionUnknownIdentifier
	 *
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testCreateEntitiesSystemException() throws Exception
	{
		Exception e = new MolgenisDataException("Check if this exception is not swallowed by the system");
		doThrow(e).when(dataService).add(eq(ENTITY_NAME), (Stream<Entity>) any(Stream.class));

		String content = "{entities:[{id:'p1', name:'Example data'}]}";
		ResultActions resultActions = mockMvc
				.perform(post(HREF_ENTITY_COLLECTION).content(content).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(header().doesNotExist("Location"));

		this.assertEqualsErrorMessage(resultActions, e.getMessage());
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
		doThrow(e).when(dataService).update(Matchers.eq(ENTITY_NAME), (Stream<Entity>) any(Stream.class));

		String content = "{entities:[{id:'p1', name:'Example data'}]}";
		ResultActions resultActions = mockMvc
				.perform(put(HREF_ENTITY_COLLECTION).content(content).contentType(APPLICATION_JSON))
				.andExpect(status().isBadRequest()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(header().doesNotExist("Location"));

		this.assertEqualsErrorMessage(resultActions, e.getMessage());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateEntitiesMolgenisValidationException() throws Exception
	{
		Exception e = new MolgenisValidationException(
				Collections.singleton(new ConstraintViolation("Message", Long.valueOf(5L))));
		doThrow(e).when(dataService).update(eq(ENTITY_NAME), (Stream<Entity>) any(Stream.class));

		String content = "{entities:[{id:'p1', name:'Example data'}]}";
		ResultActions resultActions = mockMvc
				.perform(put(HREF_ENTITY_COLLECTION).content(content).contentType(APPLICATION_JSON))
				.andExpect(status().is4xxClientError()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(header().doesNotExist("Location"));

		this.assertEqualsErrorMessage(resultActions, e.getMessage());
	}

	/**
	 * EXCEPTION_NO_ENTITIES
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateEntitiesExceptions1() throws Exception
	{
		this.testUpdateEntitiesExceptions("entity", "{entities:[]}",
				"Please provide at least one entity in the entities property.");
	}

	/**
	 * EXCEPTION_MAX_ENTITIES_EXCEEDED
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateEntitiesExceptions2() throws Exception
	{
		this.testUpdateEntitiesExceptions("entity", this.createMaxPlusOneEntitiesAsTestContent(),
				"Number of entities cannot be more than 1000.");
	}

	/**
	 * createUnknownEntityException
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateEntitiesExceptions3() throws Exception
	{
		this.testUpdateEntitiesExceptions("entity2", "{entities:[{email:'test@email.com'}]}",
				RestControllerV2.createUnknownEntityException("entity2").getMessage());

	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateEntitiesSpecificAttribute() throws Exception
	{
		String content = "{entities:[{id:'0', date_time:'1985-08-12T08:12:13+0200'}]}";
		mockMvc.perform(put(HREF_ENTITY_COLLECTION + "/date_time").content(content).contentType(APPLICATION_JSON))
				.andExpect(status().isOk());

		verify(dataService, times(1)).update(eq(ENTITY_NAME), (Stream<Entity>) any(Stream.class));

		Entity entity = dataService.findOneById(ENTITY_NAME, ENTITY_ID);
		assertEquals((new SimpleDateFormat(MolgenisDateFormat.DATEFORMAT_DATETIME)).format(entity.get("date_time")),
				"1985-08-12T08:12:13+0200");
	}

	/**
	 * EXCEPTION_NO_ENTITIES
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateEntitiesSpecificAttributeExceptions1() throws Exception
	{
		this.testUpdateEntitiesSpecificAttributeExceptions("entity", "email", "{entities:[]}",
				"Please provide at least one entity in the entities property.");
	}

	/**
	 * EXCEPTION_MAX_ENTITIES_EXCEEDED
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateEntitiesSpecificAttributeExceptions2() throws Exception
	{
		this.testUpdateEntitiesSpecificAttributeExceptions("entity", "email",
				this.createMaxPlusOneEntitiesAsTestContent(), "Number of entities cannot be more than 1000.");
	}

	/**
	 * createUnknownEntityException
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateEntitiesSpecificAttributeExceptions3() throws Exception
	{
		this.testUpdateEntitiesSpecificAttributeExceptions("entity2", "email", "{entities:[{email:'test@email.com'}]}",
				RestControllerV2.createUnknownEntityException("entity2").getMessage());
	}

	/**
	 * createUnknownAttributeException
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateEntitiesSpecificAttributeExceptions4() throws Exception
	{
		this.testUpdateEntitiesSpecificAttributeExceptions("entity", "email2", "{entities:[{email:'test@email.com'}]}",
				RestControllerV2.createUnknownAttributeException("entity", "email2").getMessage());
	}

	/**
	 * createMolgenisDataAccessExceptionReadOnlyAttribute
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateEntitiesSpecificAttributeExceptions5() throws Exception
	{
		this.testUpdateEntitiesSpecificAttributeExceptions("entity", "decimal", "{entities:[{decimal:'42'}]}",
				RestControllerV2.createMolgenisDataAccessExceptionReadOnlyAttribute("entity", "decimal").getMessage());
	}

	@Test
	public void testSelfRefWithAllAttrsEqualsSelfRefWithoutAttrs() throws Exception
	{
		MockHttpServletResponse responseWithAttrs = mockMvc
				.perform(get(RestControllerV2.BASE_URI + "/selfRefEntity/0?attrs=*").contentType(APPLICATION_JSON))
				.andReturn().getResponse();
		assertEquals(responseWithAttrs.getStatus(), 200);
		MockHttpServletResponse responseWithoutAttrs = mockMvc
				.perform(get(RestControllerV2.BASE_URI + "/selfRefEntity/0").contentType(APPLICATION_JSON)).andReturn()
				.getResponse();
		assertEquals(responseWithoutAttrs.getStatus(), 200);
		assertEquals(responseWithAttrs.getContentAsString(), responseWithoutAttrs.getContentAsString());
		Map<String, Object> lvl1 = gson
				.fromJson(responseWithAttrs.getContentAsString(), new TypeToken<Map<String, Object>>()
				{
				}.getType());
		assertEquals(lvl1.get("selfRef").toString(), "{_href=/api/v2/selfRefEntity/0, id=0}");
	}

	@Test
	public void testSelfRefWithNestedFetch() throws Exception
	{
		MockHttpServletResponse responseWithAttrs = mockMvc.perform(
				get(RestControllerV2.BASE_URI + "/selfRefEntity/0?attrs=*,selfRef(*,selfRef(*))")
						.contentType(APPLICATION_JSON)).andReturn().getResponse();
		assertEquals(responseWithAttrs.getStatus(), 200);
		Map<String, Object> lvl1 = gson
				.fromJson(responseWithAttrs.getContentAsString(), new TypeToken<Map<String, Object>>()
				{
				}.getType());
		@SuppressWarnings("unchecked") Map<String, Object> lvl2 = (Map<String, Object>) lvl1.get("selfRef");
		assertEquals(lvl2.get("selfRef").toString(),
				"{_href=/api/v2/selfRefEntity/0, id=0, selfRef={_href=/api/v2/selfRefEntity/0, id=0}}");
	}

	/**
	 * createMolgenisDataExceptionIdentifierAndValue
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateEntitiesSpecificAttributeExceptions6() throws Exception
	{
		this.testUpdateEntitiesSpecificAttributeExceptions("entity", "email",
				"{entities:[{id:0,email:'test@email.com',extraAttribute:'test'}]}",
				RestControllerV2.createMolgenisDataExceptionIdentifierAndValue().getMessage());
	}

	/**
	 * createMolgenisDataExceptionUnknownIdentifier
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateEntitiesSpecificAttributeExceptions7() throws Exception
	{
		this.testUpdateEntitiesSpecificAttributeExceptions("entity", "email",
				"{entities:[{email:'test@email.com', extraAttribute:'test'}]}",
				RestControllerV2.createMolgenisDataExceptionUnknownIdentifier(0).getMessage());
	}

	/**
	 * createUnknownEntityExceptionNotValidId
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateEntitiesSpecificAttributeExceptions8() throws Exception
	{

		this.testUpdateEntitiesSpecificAttributeExceptions("entity", "email",
				"{entities:[{id:4,email:'test@email.com'}]}",
				RestControllerV2.createUnknownEntityExceptionNotValidId("4.0").getMessage());
	}

	private void testCreateEntitiesExceptions(String entityName, String content, String message) throws Exception
	{
		ResultActions resultActions = mockMvc.perform(
				post(RestControllerV2.BASE_URI + "/" + entityName).content(content).contentType(APPLICATION_JSON));

		this.assertEqualsErrorMessage(resultActions, message);
	}

	private void testUpdateEntitiesExceptions(String entityName, String content, String message) throws Exception
	{
		ResultActions resultActions = mockMvc.perform(
				put(RestControllerV2.BASE_URI + "/" + entityName).content(content).contentType(APPLICATION_JSON));

		this.assertEqualsErrorMessage(resultActions, message);
	}

	private void testUpdateEntitiesSpecificAttributeExceptions(String entityName, String attributeName, String content,
			String message) throws Exception
	{
		ResultActions resultActions = mockMvc.perform(
				put(RestControllerV2.BASE_URI + "/" + entityName + "/" + attributeName).content(content)
						.contentType(APPLICATION_JSON));

		this.assertEqualsErrorMessage(resultActions, message);
	}

	private void assertEqualsErrorMessage(ResultActions resultActions, String message)
			throws JsonSyntaxException, UnsupportedEncodingException
	{
		MvcResult result = resultActions.andReturn();
		String contentAsString = result.getResponse().getContentAsString();
		System.out.println("content!" + contentAsString);
		Gson gson = new Gson();
		ResponseErrors errors = gson.fromJson(contentAsString, ResponseErrors.class);
		assertEquals(errors.getErrors().get(0).getMessage(), message);
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
		public MolgenisPermissionService molgenisPermissionService()
		{
			return mock(MolgenisPermissionService.class);
		}

		@Bean
		public PermissionSystemService permissionSystemService()
		{
			return mock(PermissionSystemService.class);
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
		public LanguageService languageService()
		{
			return mock(LanguageService.class);
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
		public RestControllerV2 restController()
		{
			return new RestControllerV2(dataService(), molgenisPermissionService(),
					new RestService(dataService(), idGenerator(), fileStore(), fileMetaFactory(), entityManager()),
					languageService(), permissionSystemService());
		}

	}

	private final String resourceResponse = "{\n" + "  \"_meta\": {\n" + "    \"href\": \"/api/v2/entity\",\n"
			+ "    \"hrefCollection\": \"/api/v2/entity\",\n" + "    \"name\": \"entity\",\n"
			+ "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/id\",\n" + "        \"fieldType\": \"STRING\",\n"
			+ "        \"name\": \"id\",\n" + "        \"label\": \"id\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
			+ "        \"readOnly\": true,\n" + "        \"labelAttribute\": true,\n" + "        \"unique\": true,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": true,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/bool\",\n" + "        \"fieldType\": \"BOOL\",\n"
			+ "        \"name\": \"bool\",\n" + "        \"label\": \"bool\",\n" + "        \"attributes\": [],\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/categorical\",\n"
			+ "        \"fieldType\": \"CATEGORICAL\",\n" + "        \"name\": \"categorical\",\n"
			+ "        \"label\": \"categorical\",\n" + "        \"attributes\": [],\n" + "        \"refEntity\": {\n"
			+ "          \"href\": \"/api/v2/refEntity\",\n" + "          \"hrefCollection\": \"/api/v2/refEntity\",\n"
			+ "          \"name\": \"refEntity\",\n" + "          \"label\": \"refEntity\",\n"
			+ "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": false,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": true,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": true,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [\n" + "            \"id\"\n"
			+ "          ],\n" + "          \"isAbstract\": false,\n" + "          \"writable\": false\n"
			+ "        },\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/categorical_mref\",\n"
			+ "        \"fieldType\": \"CATEGORICAL_MREF\",\n" + "        \"name\": \"categorical_mref\",\n"
			+ "        \"label\": \"categorical_mref\",\n" + "        \"attributes\": [],\n"
			+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
			+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
			+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": false,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": true,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": true,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [\n" + "            \"id\"\n"
			+ "          ],\n" + "          \"isAbstract\": false,\n" + "          \"writable\": false\n"
			+ "        },\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/compound\",\n" + "        \"fieldType\": \"COMPOUND\",\n"
			+ "        \"name\": \"compound\",\n" + "        \"label\": \"compound\",\n" + "        \"attributes\": [\n"
			+ "          {\n" + "            \"href\": \"/api/v2/entity/meta/compound_attr0\",\n"
			+ "            \"fieldType\": \"STRING\",\n" + "            \"name\": \"compound_attr0\",\n"
			+ "            \"label\": \"compound_attr0\",\n" + "            \"attributes\": [],\n"
			+ "            \"maxLength\": 255,\n" + "            \"auto\": false,\n"
			+ "            \"nillable\": false,\n" + "            \"readOnly\": false,\n"
			+ "            \"labelAttribute\": false,\n" + "            \"unique\": false,\n"
			+ "            \"visible\": true,\n" + "            \"lookupAttribute\": false,\n"
			+ "            \"aggregateable\": false\n" + "          },\n" + "          {\n"
			+ "            \"href\": \"/api/v2/entity/meta/compound_attr0Optional\",\n"
			+ "            \"fieldType\": \"STRING\",\n" + "            \"name\": \"compound_attr0Optional\",\n"
			+ "            \"label\": \"compound_attr0Optional\",\n" + "            \"attributes\": [],\n"
			+ "            \"maxLength\": 255,\n" + "            \"auto\": false,\n"
			+ "            \"nillable\": true,\n" + "            \"readOnly\": false,\n"
			+ "            \"labelAttribute\": false,\n" + "            \"unique\": false,\n"
			+ "            \"visible\": true,\n" + "            \"lookupAttribute\": false,\n"
			+ "            \"aggregateable\": false\n" + "          },\n" + "          {\n"
			+ "            \"href\": \"/api/v2/entity/meta/compound_attrcompound\",\n"
			+ "            \"fieldType\": \"COMPOUND\",\n" + "            \"name\": \"compound_attrcompound\",\n"
			+ "            \"label\": \"compound_attrcompound\",\n" + "            \"attributes\": [\n"
			+ "              {\n" + "                \"href\": \"/api/v2/entity/meta/compound_attrcompound_attr0\",\n"
			+ "                \"fieldType\": \"STRING\",\n"
			+ "                \"name\": \"compound_attrcompound_attr0\",\n"
			+ "                \"label\": \"compound_attrcompound_attr0\",\n" + "                \"attributes\": [],\n"
			+ "                \"maxLength\": 255,\n" + "                \"auto\": false,\n"
			+ "                \"nillable\": false,\n" + "                \"readOnly\": false,\n"
			+ "                \"labelAttribute\": false,\n" + "                \"unique\": false,\n"
			+ "                \"visible\": true,\n" + "                \"lookupAttribute\": false,\n"
			+ "                \"aggregateable\": false\n" + "              },\n" + "              {\n"
			+ "                \"href\": \"/api/v2/entity/meta/compound_attrcompound_attr0Optional\",\n"
			+ "                \"fieldType\": \"STRING\",\n"
			+ "                \"name\": \"compound_attrcompound_attr0Optional\",\n"
			+ "                \"label\": \"compound_attrcompound_attr0Optional\",\n"
			+ "                \"attributes\": [],\n" + "                \"maxLength\": 255,\n"
			+ "                \"auto\": false,\n" + "                \"nillable\": true,\n"
			+ "                \"readOnly\": false,\n" + "                \"labelAttribute\": false,\n"
			+ "                \"unique\": false,\n" + "                \"visible\": true,\n"
			+ "                \"lookupAttribute\": false,\n" + "                \"aggregateable\": false\n"
			+ "              }\n" + "            ],\n" + "            \"auto\": false,\n"
			+ "            \"nillable\": true,\n" + "            \"readOnly\": false,\n"
			+ "            \"labelAttribute\": false,\n" + "            \"unique\": false,\n"
			+ "            \"visible\": true,\n" + "            \"lookupAttribute\": false,\n"
			+ "            \"aggregateable\": false\n" + "          }\n" + "        ],\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/date\",\n" + "        \"fieldType\": \"DATE\",\n"
			+ "        \"name\": \"date\",\n" + "        \"label\": \"date\",\n" + "        \"attributes\": [],\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/date_time\",\n"
			+ "        \"fieldType\": \"DATE_TIME\",\n" + "        \"name\": \"date_time\",\n"
			+ "        \"label\": \"date_time\",\n" + "        \"attributes\": [],\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/decimal\",\n"
			+ "        \"fieldType\": \"DECIMAL\",\n" + "        \"name\": \"decimal\",\n"
			+ "        \"label\": \"decimal\",\n" + "        \"attributes\": [],\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": false,\n" + "        \"readOnly\": true,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/email\",\n" + "        \"fieldType\": \"EMAIL\",\n"
			+ "        \"name\": \"email\",\n" + "        \"label\": \"email\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/enum\",\n" + "        \"fieldType\": \"ENUM\",\n"
			+ "        \"name\": \"enum\",\n" + "        \"label\": \"enum\",\n" + "        \"attributes\": [],\n"
			+ "        \"enumOptions\": [\n" + "          \"enum0\",\n" + "          \"enum1\",\n"
			+ "          \"enum2\"\n" + "        ],\n" + "        \"maxLength\": 255,\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/html\",\n" + "        \"fieldType\": \"HTML\",\n"
			+ "        \"name\": \"html\",\n" + "        \"label\": \"html\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/hyperlink\",\n" + "        \"fieldType\": \"HYPERLINK\",\n"
			+ "        \"name\": \"hyperlink\",\n" + "        \"label\": \"hyperlink\",\n"
			+ "        \"attributes\": [],\n" + "        \"maxLength\": 255,\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/int\",\n" + "        \"fieldType\": \"INT\",\n"
			+ "        \"name\": \"int\",\n" + "        \"label\": \"int\",\n" + "        \"attributes\": [],\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/long\",\n" + "        \"fieldType\": \"LONG\",\n"
			+ "        \"name\": \"long\",\n" + "        \"label\": \"long\",\n" + "        \"attributes\": [],\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/mref\",\n" + "        \"fieldType\": \"MREF\",\n"
			+ "        \"name\": \"mref\",\n" + "        \"label\": \"mref\",\n" + "        \"attributes\": [],\n"
			+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
			+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
			+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": false,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": true,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": true,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [\n" + "            \"id\"\n"
			+ "          ],\n" + "          \"isAbstract\": false,\n" + "          \"writable\": false\n"
			+ "        },\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/script\",\n" + "        \"fieldType\": \"SCRIPT\",\n"
			+ "        \"name\": \"script\",\n" + "        \"label\": \"script\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/string\",\n" + "        \"fieldType\": \"STRING\",\n"
			+ "        \"name\": \"string\",\n" + "        \"label\": \"string\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/text\",\n" + "        \"fieldType\": \"TEXT\",\n"
			+ "        \"name\": \"text\",\n" + "        \"label\": \"text\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/xref\",\n" + "        \"fieldType\": \"XREF\",\n"
			+ "        \"name\": \"xref\",\n" + "        \"label\": \"xref\",\n" + "        \"attributes\": [],\n"
			+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
			+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
			+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": false,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": true,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": true,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [\n" + "            \"id\"\n"
			+ "          ],\n" + "          \"isAbstract\": false,\n" + "          \"writable\": false\n"
			+ "        },\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/boolOptional\",\n" + "        \"fieldType\": \"BOOL\",\n"
			+ "        \"name\": \"boolOptional\",\n" + "        \"label\": \"boolOptional\",\n"
			+ "        \"attributes\": [],\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/categoricalOptional\",\n"
			+ "        \"fieldType\": \"CATEGORICAL\",\n" + "        \"name\": \"categoricalOptional\",\n"
			+ "        \"label\": \"categoricalOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
			+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
			+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": false,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": true,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": true,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [\n" + "            \"id\"\n"
			+ "          ],\n" + "          \"isAbstract\": false,\n" + "          \"writable\": false\n"
			+ "        },\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/categorical_mrefOptional\",\n"
			+ "        \"fieldType\": \"CATEGORICAL_MREF\",\n" + "        \"name\": \"categorical_mrefOptional\",\n"
			+ "        \"label\": \"categorical_mrefOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
			+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
			+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": false,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": true,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": true,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [\n" + "            \"id\"\n"
			+ "          ],\n" + "          \"isAbstract\": false,\n" + "          \"writable\": false\n"
			+ "        },\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/dateOptional\",\n" + "        \"fieldType\": \"DATE\",\n"
			+ "        \"name\": \"dateOptional\",\n" + "        \"label\": \"dateOptional\",\n"
			+ "        \"attributes\": [],\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/date_timeOptional\",\n"
			+ "        \"fieldType\": \"DATE_TIME\",\n" + "        \"name\": \"date_timeOptional\",\n"
			+ "        \"label\": \"date_timeOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/decimalOptional\",\n"
			+ "        \"fieldType\": \"DECIMAL\",\n" + "        \"name\": \"decimalOptional\",\n"
			+ "        \"label\": \"decimalOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/emailOptional\",\n"
			+ "        \"fieldType\": \"EMAIL\",\n" + "        \"name\": \"emailOptional\",\n"
			+ "        \"label\": \"emailOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/enumOptional\",\n" + "        \"fieldType\": \"ENUM\",\n"
			+ "        \"name\": \"enumOptional\",\n" + "        \"label\": \"enumOptional\",\n"
			+ "        \"attributes\": [],\n" + "        \"enumOptions\": [\n" + "          \"enum0\",\n"
			+ "          \"enum1\",\n" + "          \"enum2\"\n" + "        ],\n" + "        \"maxLength\": 255,\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/htmlOptional\",\n"
			+ "        \"fieldType\": \"HTML\",\n" + "        \"name\": \"htmlOptional\",\n"
			+ "        \"label\": \"htmlOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/hyperlinkOptional\",\n"
			+ "        \"fieldType\": \"HYPERLINK\",\n" + "        \"name\": \"hyperlinkOptional\",\n"
			+ "        \"label\": \"hyperlinkOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/intOptional\",\n" + "        \"fieldType\": \"INT\",\n"
			+ "        \"name\": \"intOptional\",\n" + "        \"label\": \"intOptional\",\n"
			+ "        \"attributes\": [],\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/longOptional\",\n" + "        \"fieldType\": \"LONG\",\n"
			+ "        \"name\": \"longOptional\",\n" + "        \"label\": \"longOptional\",\n"
			+ "        \"attributes\": [],\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/mrefOptional\",\n" + "        \"fieldType\": \"MREF\",\n"
			+ "        \"name\": \"mrefOptional\",\n" + "        \"label\": \"mrefOptional\",\n"
			+ "        \"attributes\": [],\n" + "        \"refEntity\": {\n"
			+ "          \"href\": \"/api/v2/refEntity\",\n" + "          \"hrefCollection\": \"/api/v2/refEntity\",\n"
			+ "          \"name\": \"refEntity\",\n" + "          \"label\": \"refEntity\",\n"
			+ "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": false,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": true,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": true,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [\n" + "            \"id\"\n"
			+ "          ],\n" + "          \"isAbstract\": false,\n" + "          \"writable\": false\n"
			+ "        },\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/scriptOptional\",\n" + "        \"fieldType\": \"SCRIPT\",\n"
			+ "        \"name\": \"scriptOptional\",\n" + "        \"label\": \"scriptOptional\",\n"
			+ "        \"attributes\": [],\n" + "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/stringOptional\",\n"
			+ "        \"fieldType\": \"STRING\",\n" + "        \"name\": \"stringOptional\",\n"
			+ "        \"label\": \"stringOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/textOptional\",\n" + "        \"fieldType\": \"TEXT\",\n"
			+ "        \"name\": \"textOptional\",\n" + "        \"label\": \"textOptional\",\n"
			+ "        \"attributes\": [],\n" + "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/xrefOptional\",\n"
			+ "        \"fieldType\": \"XREF\",\n" + "        \"name\": \"xrefOptional\",\n"
			+ "        \"label\": \"xrefOptional\",\n" + "        \"attributes\": [],\n" + "        \"refEntity\": {\n"
			+ "          \"href\": \"/api/v2/refEntity\",\n" + "          \"hrefCollection\": \"/api/v2/refEntity\",\n"
			+ "          \"name\": \"refEntity\",\n" + "          \"label\": \"refEntity\",\n"
			+ "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": false,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": true,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": true,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [\n" + "            \"id\"\n"
			+ "          ],\n" + "          \"isAbstract\": false,\n" + "          \"writable\": false\n"
			+ "        },\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      }\n" + "    ],\n" + "    \"labelAttribute\": \"id\",\n"
			+ "    \"idAttribute\": \"id\",\n" + "    \"lookupAttributes\": [\n" + "      \"id\"\n" + "    ],\n"
			+ "    \"isAbstract\": false,\n" + "    \"writable\": false\n" + "  },\n"
			+ "  \"_href\": \"/api/v2/entity/0\",\n" + "  \"id\": \"0\",\n" + "  \"bool\": true,\n"
			+ "  \"categorical\": {\n" + "    \"_href\": \"/api/v2/refEntity/ref0\",\n" + "    \"id\": \"ref0\"\n"
			+ "  },\n" + "  \"categorical_mref\": [\n" + "    {\n" + "      \"_href\": \"/api/v2/refEntity/ref0\",\n"
			+ "      \"id\": \"ref0\"\n" + "    },\n" + "    {\n" + "      \"_href\": \"/api/v2/refEntity/ref0\",\n"
			+ "      \"id\": \"ref0\"\n" + "    }\n" + "  ],\n" + "  \"compound_attr0\": \"compoundAttr0Str\",\n"
			+ "  \"compound_attrcompound_attr0\": \"compoundAttrCompoundAttr0Str\",\n" + "  \"date\": \"2015-05-22\",\n"
			+ "  \"date_time\": \"2015-05-22T08:12:13+0200\",\n" + "  \"decimal\": 3.14,\n"
			+ "  \"email\": \"my@mail.com\",\n" + "  \"enum\": \"enum0\",\n" + "  \"html\": \"<h1>html</h1>\",\n"
			+ "  \"hyperlink\": \"http://www.molgenis.org/\",\n" + "  \"int\": 123,\n"
			+ "  \"long\": 9223372036854775807,\n" + "  \"mref\": [\n" + "    {\n"
			+ "      \"_href\": \"/api/v2/refEntity/ref0\",\n" + "      \"id\": \"ref0\"\n" + "    },\n" + "    {\n"
			+ "      \"_href\": \"/api/v2/refEntity/ref0\",\n" + "      \"id\": \"ref0\"\n" + "    }\n" + "  ],\n"
			+ "  \"script\": \"print \\\"Hello world\\\"\",\n" + "  \"string\": \"str\",\n"
			+ "  \"text\": \"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam consectetur auctor lectus sed tincidunt. Fusce sodales quis mauris non aliquam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Integer maximus imperdiet velit quis vehicula. Mauris pulvinar amet.\",\n"
			+ "  \"xref\": {\n" + "    \"_href\": \"/api/v2/refEntity/ref0\",\n" + "    \"id\": \"ref0\"\n" + "  },\n"
			+ "  \"categorical_mrefOptional\": [],\n" + "  \"mrefOptional\": []\n" + "}";

	private final String resourcePartialAttributeResponse =
			"{\n" + "  \"_meta\": {\n" + "    \"href\": \"/api/v2/entity\",\n"
					+ "    \"hrefCollection\": \"/api/v2/entity\",\n" + "    \"name\": \"entity\",\n"
					+ "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/bool\",\n" + "        \"fieldType\": \"BOOL\",\n"
					+ "        \"name\": \"bool\",\n" + "        \"label\": \"bool\",\n"
					+ "        \"attributes\": [],\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
					+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      }\n"
					+ "    ],\n" + "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n"
					+ "    \"lookupAttributes\": [\n" + "      \"id\"\n" + "    ],\n" + "    \"isAbstract\": false,\n"
					+ "    \"writable\": false\n" + "  },\n" + "  \"_href\": \"/api/v2/entity/0\",\n"
					+ "  \"bool\": true\n" + "}";

	private final String resourcePartialAttributeInCompoundResponse =
			"{\n" + "  \"_meta\": {\n" + "    \"href\": \"/api/v2/entity\",\n"
					+ "    \"hrefCollection\": \"/api/v2/entity\",\n" + "    \"name\": \"entity\",\n"
					+ "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/compound\",\n" + "        \"fieldType\": \"COMPOUND\",\n"
					+ "        \"name\": \"compound\",\n" + "        \"label\": \"compound\",\n"
					+ "        \"attributes\": [\n" + "          {\n"
					+ "            \"href\": \"/api/v2/entity/meta/compound_attr0\",\n"
					+ "            \"fieldType\": \"STRING\",\n" + "            \"name\": \"compound_attr0\",\n"
					+ "            \"label\": \"compound_attr0\",\n" + "            \"attributes\": [],\n"
					+ "            \"maxLength\": 255,\n" + "            \"auto\": false,\n"
					+ "            \"nillable\": false,\n" + "            \"readOnly\": false,\n"
					+ "            \"labelAttribute\": false,\n" + "            \"unique\": false,\n"
					+ "            \"visible\": true,\n" + "            \"lookupAttribute\": false,\n"
					+ "            \"aggregateable\": false\n" + "          }\n" + "        ],\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      }\n" + "    ],\n"
					+ "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n"
					+ "    \"lookupAttributes\": [\n" + "      \"id\"\n" + "    ],\n" + "    \"isAbstract\": false,\n"
					+ "    \"writable\": false\n" + "  },\n" + "  \"_href\": \"/api/v2/entity/0\",\n"
					+ "  \"compound_attr0\": \"compoundAttr0Str\"\n" + "}";

	private final String resourcePartialAttributeInCompoundInCompoundResponse =
			"{\n" + "  \"_meta\": {\n" + "    \"href\": \"/api/v2/entity\",\n"
					+ "    \"hrefCollection\": \"/api/v2/entity\",\n" + "    \"name\": \"entity\",\n"
					+ "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/compound\",\n" + "        \"fieldType\": \"COMPOUND\",\n"
					+ "        \"name\": \"compound\",\n" + "        \"label\": \"compound\",\n"
					+ "        \"attributes\": [\n" + "          {\n"
					+ "            \"href\": \"/api/v2/entity/meta/compound_attrcompound\",\n"
					+ "            \"fieldType\": \"COMPOUND\",\n"
					+ "            \"name\": \"compound_attrcompound\",\n"
					+ "            \"label\": \"compound_attrcompound\",\n" + "            \"attributes\": [\n"
					+ "              {\n"
					+ "                \"href\": \"/api/v2/entity/meta/compound_attrcompound_attr0\",\n"
					+ "                \"fieldType\": \"STRING\",\n"
					+ "                \"name\": \"compound_attrcompound_attr0\",\n"
					+ "                \"label\": \"compound_attrcompound_attr0\",\n"
					+ "                \"attributes\": [],\n" + "                \"maxLength\": 255,\n"
					+ "                \"auto\": false,\n" + "                \"nillable\": false,\n"
					+ "                \"readOnly\": false,\n" + "                \"labelAttribute\": false,\n"
					+ "                \"unique\": false,\n" + "                \"visible\": true,\n"
					+ "                \"lookupAttribute\": false,\n" + "                \"aggregateable\": false\n"
					+ "              }\n" + "            ],\n" + "            \"auto\": false,\n"
					+ "            \"nillable\": true,\n" + "            \"readOnly\": false,\n"
					+ "            \"labelAttribute\": false,\n" + "            \"unique\": false,\n"
					+ "            \"visible\": true,\n" + "            \"lookupAttribute\": false,\n"
					+ "            \"aggregateable\": false\n" + "          }\n" + "        ],\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      }\n" + "    ],\n"
					+ "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n"
					+ "    \"lookupAttributes\": [\n" + "      \"id\"\n" + "    ],\n" + "    \"isAbstract\": false,\n"
					+ "    \"writable\": false\n" + "  },\n" + "  \"_href\": \"/api/v2/entity/0\",\n"
					+ "  \"compound_attrcompound_attr0\": \"compoundAttrCompoundAttr0Str\"\n" + "}";

	private final String resourcePartialSubAttributeResponse =
			"{\n" + "  \"_meta\": {\n" + "    \"href\": \"/api/v2/entity\",\n"
					+ "    \"hrefCollection\": \"/api/v2/entity\",\n" + "    \"name\": \"entity\",\n"
					+ "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/xref\",\n" + "        \"fieldType\": \"XREF\",\n"
					+ "        \"name\": \"xref\",\n" + "        \"label\": \"xref\",\n"
					+ "        \"attributes\": [],\n" + "        \"refEntity\": {\n"
					+ "          \"href\": \"/api/v2/refEntity\",\n"
					+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
					+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
					+ "              \"href\": \"/api/v2/refEntity/meta/value\",\n"
					+ "              \"fieldType\": \"STRING\",\n" + "              \"name\": \"value\",\n"
					+ "              \"label\": \"value\",\n" + "              \"attributes\": [],\n"
					+ "              \"maxLength\": 255,\n" + "              \"auto\": false,\n"
					+ "              \"nillable\": true,\n" + "              \"readOnly\": false,\n"
					+ "              \"labelAttribute\": false,\n" + "              \"unique\": false,\n"
					+ "              \"visible\": true,\n" + "              \"lookupAttribute\": false,\n"
					+ "              \"aggregateable\": false\n" + "            }\n" + "          ],\n"
					+ "          \"labelAttribute\": \"id\",\n" + "          \"idAttribute\": \"id\",\n"
					+ "          \"lookupAttributes\": [\n" + "            \"id\"\n" + "          ],\n"
					+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      }\n" + "    ],\n"
					+ "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n"
					+ "    \"lookupAttributes\": [\n" + "      \"id\"\n" + "    ],\n" + "    \"isAbstract\": false,\n"
					+ "    \"writable\": false\n" + "  },\n" + "  \"_href\": \"/api/v2/entity/0\",\n"
					+ "  \"xref\": {\n" + "    \"_href\": \"/api/v2/refEntity/ref0\",\n" + "    \"value\": \"val0\"\n"
					+ "  }\n" + "}";

	private final String resourcePartialSubAttributesResponse =
			"{\n" + "  \"_meta\": {\n" + "    \"href\": \"/api/v2/entity\",\n"
					+ "    \"hrefCollection\": \"/api/v2/entity\",\n" + "    \"name\": \"entity\",\n"
					+ "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/xref\",\n" + "        \"fieldType\": \"XREF\",\n"
					+ "        \"name\": \"xref\",\n" + "        \"label\": \"xref\",\n"
					+ "        \"attributes\": [],\n" + "        \"refEntity\": {\n"
					+ "          \"href\": \"/api/v2/refEntity\",\n"
					+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
					+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
					+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n"
					+ "              \"fieldType\": \"STRING\",\n" + "              \"name\": \"id\",\n"
					+ "              \"label\": \"id\",\n" + "              \"attributes\": [],\n"
					+ "              \"maxLength\": 255,\n" + "              \"auto\": false,\n"
					+ "              \"nillable\": false,\n" + "              \"readOnly\": true,\n"
					+ "              \"labelAttribute\": true,\n" + "              \"unique\": true,\n"
					+ "              \"visible\": true,\n" + "              \"lookupAttribute\": true,\n"
					+ "              \"aggregateable\": false\n" + "            },\n" + "            {\n"
					+ "              \"href\": \"/api/v2/refEntity/meta/value\",\n"
					+ "              \"fieldType\": \"STRING\",\n" + "              \"name\": \"value\",\n"
					+ "              \"label\": \"value\",\n" + "              \"attributes\": [],\n"
					+ "              \"maxLength\": 255,\n" + "              \"auto\": false,\n"
					+ "              \"nillable\": true,\n" + "              \"readOnly\": false,\n"
					+ "              \"labelAttribute\": false,\n" + "              \"unique\": false,\n"
					+ "              \"visible\": true,\n" + "              \"lookupAttribute\": false,\n"
					+ "              \"aggregateable\": false\n" + "            }\n" + "          ],\n"
					+ "          \"labelAttribute\": \"id\",\n" + "          \"idAttribute\": \"id\",\n"
					+ "          \"lookupAttributes\": [\n" + "            \"id\"\n" + "          ],\n"
					+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      }\n" + "    ],\n"
					+ "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n"
					+ "    \"lookupAttributes\": [\n" + "      \"id\"\n" + "    ],\n" + "    \"isAbstract\": false,\n"
					+ "    \"writable\": false\n" + "  },\n" + "  \"_href\": \"/api/v2/entity/0\",\n"
					+ "  \"xref\": {\n" + "    \"_href\": \"/api/v2/refEntity/ref0\",\n" + "    \"id\": \"ref0\",\n"
					+ "    \"value\": \"val0\"\n" + "  }\n" + "}";

	private final String resourcePartialAttributesResponse =
			"{\n" + "  \"_meta\": {\n" + "    \"href\": \"/api/v2/entity\",\n"
					+ "    \"hrefCollection\": \"/api/v2/entity\",\n" + "    \"name\": \"entity\",\n"
					+ "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/bool\",\n" + "        \"fieldType\": \"BOOL\",\n"
					+ "        \"name\": \"bool\",\n" + "        \"label\": \"bool\",\n"
					+ "        \"attributes\": [],\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
					+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
					+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/string\",\n"
					+ "        \"fieldType\": \"STRING\",\n" + "        \"name\": \"string\",\n"
					+ "        \"label\": \"string\",\n" + "        \"attributes\": [],\n"
					+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
					+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      }\n"
					+ "    ],\n" + "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n"
					+ "    \"lookupAttributes\": [\n" + "      \"id\"\n" + "    ],\n" + "    \"isAbstract\": false,\n"
					+ "    \"writable\": false\n" + "  },\n" + "  \"_href\": \"/api/v2/entity/0\",\n"
					+ "  \"bool\": true,\n" + "  \"string\": \"str\"\n" + "}";

	private final String resourceCollectionResponse =
			"{\n" + "  \"href\": \"/api/v2/entity\",\n" + "  \"meta\": {\n" + "    \"href\": \"/api/v2/entity\",\n"
					+ "    \"hrefCollection\": \"/api/v2/entity\",\n" + "    \"name\": \"entity\",\n"
					+ "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/id\",\n" + "        \"fieldType\": \"STRING\",\n"
					+ "        \"name\": \"id\",\n" + "        \"label\": \"id\",\n" + "        \"attributes\": [],\n"
					+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
					+ "        \"readOnly\": true,\n" + "        \"labelAttribute\": true,\n"
					+ "        \"unique\": true,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": true,\n" + "        \"aggregateable\": false\n" + "      },\n"
					+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/bool\",\n"
					+ "        \"fieldType\": \"BOOL\",\n" + "        \"name\": \"bool\",\n"
					+ "        \"label\": \"bool\",\n" + "        \"attributes\": [],\n" + "        \"auto\": false,\n"
					+ "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/categorical\",\n"
					+ "        \"fieldType\": \"CATEGORICAL\",\n" + "        \"name\": \"categorical\",\n"
					+ "        \"label\": \"categorical\",\n" + "        \"attributes\": [],\n"
					+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
					+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
					+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
					+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n"
					+ "              \"fieldType\": \"STRING\",\n" + "              \"name\": \"id\",\n"
					+ "              \"label\": \"id\",\n" + "              \"attributes\": [],\n"
					+ "              \"maxLength\": 255,\n" + "              \"auto\": false,\n"
					+ "              \"nillable\": false,\n" + "              \"readOnly\": true,\n"
					+ "              \"labelAttribute\": true,\n" + "              \"unique\": true,\n"
					+ "              \"visible\": true,\n" + "              \"lookupAttribute\": true,\n"
					+ "              \"aggregateable\": false\n" + "            }\n" + "          ],\n"
					+ "          \"labelAttribute\": \"id\",\n" + "          \"idAttribute\": \"id\",\n"
					+ "          \"lookupAttributes\": [\n" + "            \"id\"\n" + "          ],\n"
					+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/categorical_mref\",\n"
					+ "        \"fieldType\": \"CATEGORICAL_MREF\",\n" + "        \"name\": \"categorical_mref\",\n"
					+ "        \"label\": \"categorical_mref\",\n" + "        \"attributes\": [],\n"
					+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
					+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
					+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
					+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n"
					+ "              \"fieldType\": \"STRING\",\n" + "              \"name\": \"id\",\n"
					+ "              \"label\": \"id\",\n" + "              \"attributes\": [],\n"
					+ "              \"maxLength\": 255,\n" + "              \"auto\": false,\n"
					+ "              \"nillable\": false,\n" + "              \"readOnly\": true,\n"
					+ "              \"labelAttribute\": true,\n" + "              \"unique\": true,\n"
					+ "              \"visible\": true,\n" + "              \"lookupAttribute\": true,\n"
					+ "              \"aggregateable\": false\n" + "            }\n" + "          ],\n"
					+ "          \"labelAttribute\": \"id\",\n" + "          \"idAttribute\": \"id\",\n"
					+ "          \"lookupAttributes\": [\n" + "            \"id\"\n" + "          ],\n"
					+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/compound\",\n" + "        \"fieldType\": \"COMPOUND\",\n"
					+ "        \"name\": \"compound\",\n" + "        \"label\": \"compound\",\n"
					+ "        \"attributes\": [\n" + "          {\n"
					+ "            \"href\": \"/api/v2/entity/meta/compound_attr0\",\n"
					+ "            \"fieldType\": \"STRING\",\n" + "            \"name\": \"compound_attr0\",\n"
					+ "            \"label\": \"compound_attr0\",\n" + "            \"attributes\": [],\n"
					+ "            \"maxLength\": 255,\n" + "            \"auto\": false,\n"
					+ "            \"nillable\": false,\n" + "            \"readOnly\": false,\n"
					+ "            \"labelAttribute\": false,\n" + "            \"unique\": false,\n"
					+ "            \"visible\": true,\n" + "            \"lookupAttribute\": false,\n"
					+ "            \"aggregateable\": false\n" + "          },\n" + "          {\n"
					+ "            \"href\": \"/api/v2/entity/meta/compound_attr0Optional\",\n"
					+ "            \"fieldType\": \"STRING\",\n" + "            \"name\": \"compound_attr0Optional\",\n"
					+ "            \"label\": \"compound_attr0Optional\",\n" + "            \"attributes\": [],\n"
					+ "            \"maxLength\": 255,\n" + "            \"auto\": false,\n"
					+ "            \"nillable\": true,\n" + "            \"readOnly\": false,\n"
					+ "            \"labelAttribute\": false,\n" + "            \"unique\": false,\n"
					+ "            \"visible\": true,\n" + "            \"lookupAttribute\": false,\n"
					+ "            \"aggregateable\": false\n" + "          },\n" + "          {\n"
					+ "            \"href\": \"/api/v2/entity/meta/compound_attrcompound\",\n"
					+ "            \"fieldType\": \"COMPOUND\",\n"
					+ "            \"name\": \"compound_attrcompound\",\n"
					+ "            \"label\": \"compound_attrcompound\",\n" + "            \"attributes\": [\n"
					+ "              {\n"
					+ "                \"href\": \"/api/v2/entity/meta/compound_attrcompound_attr0\",\n"
					+ "                \"fieldType\": \"STRING\",\n"
					+ "                \"name\": \"compound_attrcompound_attr0\",\n"
					+ "                \"label\": \"compound_attrcompound_attr0\",\n"
					+ "                \"attributes\": [],\n" + "                \"maxLength\": 255,\n"
					+ "                \"auto\": false,\n" + "                \"nillable\": false,\n"
					+ "                \"readOnly\": false,\n" + "                \"labelAttribute\": false,\n"
					+ "                \"unique\": false,\n" + "                \"visible\": true,\n"
					+ "                \"lookupAttribute\": false,\n" + "                \"aggregateable\": false\n"
					+ "              },\n" + "              {\n"
					+ "                \"href\": \"/api/v2/entity/meta/compound_attrcompound_attr0Optional\",\n"
					+ "                \"fieldType\": \"STRING\",\n"
					+ "                \"name\": \"compound_attrcompound_attr0Optional\",\n"
					+ "                \"label\": \"compound_attrcompound_attr0Optional\",\n"
					+ "                \"attributes\": [],\n" + "                \"maxLength\": 255,\n"
					+ "                \"auto\": false,\n" + "                \"nillable\": true,\n"
					+ "                \"readOnly\": false,\n" + "                \"labelAttribute\": false,\n"
					+ "                \"unique\": false,\n" + "                \"visible\": true,\n"
					+ "                \"lookupAttribute\": false,\n" + "                \"aggregateable\": false\n"
					+ "              }\n" + "            ],\n" + "            \"auto\": false,\n"
					+ "            \"nillable\": true,\n" + "            \"readOnly\": false,\n"
					+ "            \"labelAttribute\": false,\n" + "            \"unique\": false,\n"
					+ "            \"visible\": true,\n" + "            \"lookupAttribute\": false,\n"
					+ "            \"aggregateable\": false\n" + "          }\n" + "        ],\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/date\",\n" + "        \"fieldType\": \"DATE\",\n"
					+ "        \"name\": \"date\",\n" + "        \"label\": \"date\",\n"
					+ "        \"attributes\": [],\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
					+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
					+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/date_time\",\n"
					+ "        \"fieldType\": \"DATE_TIME\",\n" + "        \"name\": \"date_time\",\n"
					+ "        \"label\": \"date_time\",\n" + "        \"attributes\": [],\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/decimal\",\n" + "        \"fieldType\": \"DECIMAL\",\n"
					+ "        \"name\": \"decimal\",\n" + "        \"label\": \"decimal\",\n"
					+ "        \"attributes\": [],\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
					+ "        \"readOnly\": true,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
					+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/email\",\n"
					+ "        \"fieldType\": \"EMAIL\",\n" + "        \"name\": \"email\",\n"
					+ "        \"label\": \"email\",\n" + "        \"attributes\": [],\n"
					+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
					+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
					+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/enum\",\n"
					+ "        \"fieldType\": \"ENUM\",\n" + "        \"name\": \"enum\",\n"
					+ "        \"label\": \"enum\",\n" + "        \"attributes\": [],\n"
					+ "        \"enumOptions\": [\n" + "          \"enum0\",\n" + "          \"enum1\",\n"
					+ "          \"enum2\"\n" + "        ],\n" + "        \"maxLength\": 255,\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/html\",\n" + "        \"fieldType\": \"HTML\",\n"
					+ "        \"name\": \"html\",\n" + "        \"label\": \"html\",\n"
					+ "        \"attributes\": [],\n" + "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n"
					+ "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/hyperlink\",\n"
					+ "        \"fieldType\": \"HYPERLINK\",\n" + "        \"name\": \"hyperlink\",\n"
					+ "        \"label\": \"hyperlink\",\n" + "        \"attributes\": [],\n"
					+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
					+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
					+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/int\",\n"
					+ "        \"fieldType\": \"INT\",\n" + "        \"name\": \"int\",\n"
					+ "        \"label\": \"int\",\n" + "        \"attributes\": [],\n" + "        \"auto\": false,\n"
					+ "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/long\",\n" + "        \"fieldType\": \"LONG\",\n"
					+ "        \"name\": \"long\",\n" + "        \"label\": \"long\",\n"
					+ "        \"attributes\": [],\n" + "        \"auto\": false,\n" + "        \"nillable\": false,\n"
					+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
					+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/mref\",\n"
					+ "        \"fieldType\": \"MREF\",\n" + "        \"name\": \"mref\",\n"
					+ "        \"label\": \"mref\",\n" + "        \"attributes\": [],\n" + "        \"refEntity\": {\n"
					+ "          \"href\": \"/api/v2/refEntity\",\n"
					+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
					+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
					+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n"
					+ "              \"fieldType\": \"STRING\",\n" + "              \"name\": \"id\",\n"
					+ "              \"label\": \"id\",\n" + "              \"attributes\": [],\n"
					+ "              \"maxLength\": 255,\n" + "              \"auto\": false,\n"
					+ "              \"nillable\": false,\n" + "              \"readOnly\": true,\n"
					+ "              \"labelAttribute\": true,\n" + "              \"unique\": true,\n"
					+ "              \"visible\": true,\n" + "              \"lookupAttribute\": true,\n"
					+ "              \"aggregateable\": false\n" + "            }\n" + "          ],\n"
					+ "          \"labelAttribute\": \"id\",\n" + "          \"idAttribute\": \"id\",\n"
					+ "          \"lookupAttributes\": [\n" + "            \"id\"\n" + "          ],\n"
					+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/script\",\n" + "        \"fieldType\": \"SCRIPT\",\n"
					+ "        \"name\": \"script\",\n" + "        \"label\": \"script\",\n"
					+ "        \"attributes\": [],\n" + "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n"
					+ "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/string\",\n" + "        \"fieldType\": \"STRING\",\n"
					+ "        \"name\": \"string\",\n" + "        \"label\": \"string\",\n"
					+ "        \"attributes\": [],\n" + "        \"maxLength\": 255,\n" + "        \"auto\": false,\n"
					+ "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/text\",\n" + "        \"fieldType\": \"TEXT\",\n"
					+ "        \"name\": \"text\",\n" + "        \"label\": \"text\",\n"
					+ "        \"attributes\": [],\n" + "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n"
					+ "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/xref\",\n" + "        \"fieldType\": \"XREF\",\n"
					+ "        \"name\": \"xref\",\n" + "        \"label\": \"xref\",\n"
					+ "        \"attributes\": [],\n" + "        \"refEntity\": {\n"
					+ "          \"href\": \"/api/v2/refEntity\",\n"
					+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
					+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
					+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n"
					+ "              \"fieldType\": \"STRING\",\n" + "              \"name\": \"id\",\n"
					+ "              \"label\": \"id\",\n" + "              \"attributes\": [],\n"
					+ "              \"maxLength\": 255,\n" + "              \"auto\": false,\n"
					+ "              \"nillable\": false,\n" + "              \"readOnly\": true,\n"
					+ "              \"labelAttribute\": true,\n" + "              \"unique\": true,\n"
					+ "              \"visible\": true,\n" + "              \"lookupAttribute\": true,\n"
					+ "              \"aggregateable\": false\n" + "            }\n" + "          ],\n"
					+ "          \"labelAttribute\": \"id\",\n" + "          \"idAttribute\": \"id\",\n"
					+ "          \"lookupAttributes\": [\n" + "            \"id\"\n" + "          ],\n"
					+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/boolOptional\",\n" + "        \"fieldType\": \"BOOL\",\n"
					+ "        \"name\": \"boolOptional\",\n" + "        \"label\": \"boolOptional\",\n"
					+ "        \"attributes\": [],\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
					+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
					+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/categoricalOptional\",\n"
					+ "        \"fieldType\": \"CATEGORICAL\",\n" + "        \"name\": \"categoricalOptional\",\n"
					+ "        \"label\": \"categoricalOptional\",\n" + "        \"attributes\": [],\n"
					+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
					+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
					+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
					+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n"
					+ "              \"fieldType\": \"STRING\",\n" + "              \"name\": \"id\",\n"
					+ "              \"label\": \"id\",\n" + "              \"attributes\": [],\n"
					+ "              \"maxLength\": 255,\n" + "              \"auto\": false,\n"
					+ "              \"nillable\": false,\n" + "              \"readOnly\": true,\n"
					+ "              \"labelAttribute\": true,\n" + "              \"unique\": true,\n"
					+ "              \"visible\": true,\n" + "              \"lookupAttribute\": true,\n"
					+ "              \"aggregateable\": false\n" + "            }\n" + "          ],\n"
					+ "          \"labelAttribute\": \"id\",\n" + "          \"idAttribute\": \"id\",\n"
					+ "          \"lookupAttributes\": [\n" + "            \"id\"\n" + "          ],\n"
					+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/categorical_mrefOptional\",\n"
					+ "        \"fieldType\": \"CATEGORICAL_MREF\",\n"
					+ "        \"name\": \"categorical_mrefOptional\",\n"
					+ "        \"label\": \"categorical_mrefOptional\",\n" + "        \"attributes\": [],\n"
					+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
					+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
					+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
					+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n"
					+ "              \"fieldType\": \"STRING\",\n" + "              \"name\": \"id\",\n"
					+ "              \"label\": \"id\",\n" + "              \"attributes\": [],\n"
					+ "              \"maxLength\": 255,\n" + "              \"auto\": false,\n"
					+ "              \"nillable\": false,\n" + "              \"readOnly\": true,\n"
					+ "              \"labelAttribute\": true,\n" + "              \"unique\": true,\n"
					+ "              \"visible\": true,\n" + "              \"lookupAttribute\": true,\n"
					+ "              \"aggregateable\": false\n" + "            }\n" + "          ],\n"
					+ "          \"labelAttribute\": \"id\",\n" + "          \"idAttribute\": \"id\",\n"
					+ "          \"lookupAttributes\": [\n" + "            \"id\"\n" + "          ],\n"
					+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/dateOptional\",\n" + "        \"fieldType\": \"DATE\",\n"
					+ "        \"name\": \"dateOptional\",\n" + "        \"label\": \"dateOptional\",\n"
					+ "        \"attributes\": [],\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
					+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
					+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/date_timeOptional\",\n"
					+ "        \"fieldType\": \"DATE_TIME\",\n" + "        \"name\": \"date_timeOptional\",\n"
					+ "        \"label\": \"date_timeOptional\",\n" + "        \"attributes\": [],\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/decimalOptional\",\n"
					+ "        \"fieldType\": \"DECIMAL\",\n" + "        \"name\": \"decimalOptional\",\n"
					+ "        \"label\": \"decimalOptional\",\n" + "        \"attributes\": [],\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/emailOptional\",\n"
					+ "        \"fieldType\": \"EMAIL\",\n" + "        \"name\": \"emailOptional\",\n"
					+ "        \"label\": \"emailOptional\",\n" + "        \"attributes\": [],\n"
					+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
					+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
					+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/enumOptional\",\n"
					+ "        \"fieldType\": \"ENUM\",\n" + "        \"name\": \"enumOptional\",\n"
					+ "        \"label\": \"enumOptional\",\n" + "        \"attributes\": [],\n"
					+ "        \"enumOptions\": [\n" + "          \"enum0\",\n" + "          \"enum1\",\n"
					+ "          \"enum2\"\n" + "        ],\n" + "        \"maxLength\": 255,\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/htmlOptional\",\n" + "        \"fieldType\": \"HTML\",\n"
					+ "        \"name\": \"htmlOptional\",\n" + "        \"label\": \"htmlOptional\",\n"
					+ "        \"attributes\": [],\n" + "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n"
					+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/hyperlinkOptional\",\n"
					+ "        \"fieldType\": \"HYPERLINK\",\n" + "        \"name\": \"hyperlinkOptional\",\n"
					+ "        \"label\": \"hyperlinkOptional\",\n" + "        \"attributes\": [],\n"
					+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
					+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
					+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/intOptional\",\n"
					+ "        \"fieldType\": \"INT\",\n" + "        \"name\": \"intOptional\",\n"
					+ "        \"label\": \"intOptional\",\n" + "        \"attributes\": [],\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/longOptional\",\n" + "        \"fieldType\": \"LONG\",\n"
					+ "        \"name\": \"longOptional\",\n" + "        \"label\": \"longOptional\",\n"
					+ "        \"attributes\": [],\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
					+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
					+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/mrefOptional\",\n"
					+ "        \"fieldType\": \"MREF\",\n" + "        \"name\": \"mrefOptional\",\n"
					+ "        \"label\": \"mrefOptional\",\n" + "        \"attributes\": [],\n"
					+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
					+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
					+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
					+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n"
					+ "              \"fieldType\": \"STRING\",\n" + "              \"name\": \"id\",\n"
					+ "              \"label\": \"id\",\n" + "              \"attributes\": [],\n"
					+ "              \"maxLength\": 255,\n" + "              \"auto\": false,\n"
					+ "              \"nillable\": false,\n" + "              \"readOnly\": true,\n"
					+ "              \"labelAttribute\": true,\n" + "              \"unique\": true,\n"
					+ "              \"visible\": true,\n" + "              \"lookupAttribute\": true,\n"
					+ "              \"aggregateable\": false\n" + "            }\n" + "          ],\n"
					+ "          \"labelAttribute\": \"id\",\n" + "          \"idAttribute\": \"id\",\n"
					+ "          \"lookupAttributes\": [\n" + "            \"id\"\n" + "          ],\n"
					+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/scriptOptional\",\n"
					+ "        \"fieldType\": \"SCRIPT\",\n" + "        \"name\": \"scriptOptional\",\n"
					+ "        \"label\": \"scriptOptional\",\n" + "        \"attributes\": [],\n"
					+ "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
					+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
					+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/stringOptional\",\n"
					+ "        \"fieldType\": \"STRING\",\n" + "        \"name\": \"stringOptional\",\n"
					+ "        \"label\": \"stringOptional\",\n" + "        \"attributes\": [],\n"
					+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
					+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
					+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/textOptional\",\n"
					+ "        \"fieldType\": \"TEXT\",\n" + "        \"name\": \"textOptional\",\n"
					+ "        \"label\": \"textOptional\",\n" + "        \"attributes\": [],\n"
					+ "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
					+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n"
					+ "        \"unique\": false,\n" + "        \"visible\": true,\n"
					+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
					+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/xrefOptional\",\n"
					+ "        \"fieldType\": \"XREF\",\n" + "        \"name\": \"xrefOptional\",\n"
					+ "        \"label\": \"xrefOptional\",\n" + "        \"attributes\": [],\n"
					+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
					+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
					+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
					+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n"
					+ "              \"fieldType\": \"STRING\",\n" + "              \"name\": \"id\",\n"
					+ "              \"label\": \"id\",\n" + "              \"attributes\": [],\n"
					+ "              \"maxLength\": 255,\n" + "              \"auto\": false,\n"
					+ "              \"nillable\": false,\n" + "              \"readOnly\": true,\n"
					+ "              \"labelAttribute\": true,\n" + "              \"unique\": true,\n"
					+ "              \"visible\": true,\n" + "              \"lookupAttribute\": true,\n"
					+ "              \"aggregateable\": false\n" + "            }\n" + "          ],\n"
					+ "          \"labelAttribute\": \"id\",\n" + "          \"idAttribute\": \"id\",\n"
					+ "          \"lookupAttributes\": [\n" + "            \"id\"\n" + "          ],\n"
					+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
					+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      }\n" + "    ],\n"
					+ "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n"
					+ "    \"lookupAttributes\": [\n" + "      \"id\"\n" + "    ],\n" + "    \"isAbstract\": false,\n"
					+ "    \"writable\": false\n" + "  },\n" + "  \"start\": 0,\n" + "  \"num\": 100,\n"
					+ "  \"total\": 2,\n" + "  \"items\": [\n" + "    {\n" + "      \"_href\": \"/api/v2/entity/0\",\n"
					+ "      \"id\": \"0\",\n" + "      \"bool\": true,\n" + "      \"categorical\": {\n"
					+ "        \"_href\": \"/api/v2/refEntity/ref0\",\n" + "        \"id\": \"ref0\"\n" + "      },\n"
					+ "      \"categorical_mref\": [\n" + "        {\n"
					+ "          \"_href\": \"/api/v2/refEntity/ref0\",\n" + "          \"id\": \"ref0\"\n"
					+ "        },\n" + "        {\n" + "          \"_href\": \"/api/v2/refEntity/ref0\",\n"
					+ "          \"id\": \"ref0\"\n" + "        }\n" + "      ],\n"
					+ "      \"compound_attr0\": \"compoundAttr0Str\",\n"
					+ "      \"compound_attrcompound_attr0\": \"compoundAttrCompoundAttr0Str\",\n"
					+ "      \"date\": \"2015-05-22\",\n" + "      \"date_time\": \"2015-05-22T08:12:13+0200\",\n"
					+ "      \"decimal\": 3.14,\n" + "      \"email\": \"my@mail.com\",\n"
					+ "      \"enum\": \"enum0\",\n" + "      \"html\": \"<h1>html</h1>\",\n"
					+ "      \"hyperlink\": \"http://www.molgenis.org/\",\n" + "      \"int\": 123,\n"
					+ "      \"long\": 9223372036854775807,\n" + "      \"mref\": [\n" + "        {\n"
					+ "          \"_href\": \"/api/v2/refEntity/ref0\",\n" + "          \"id\": \"ref0\"\n"
					+ "        },\n" + "        {\n" + "          \"_href\": \"/api/v2/refEntity/ref0\",\n"
					+ "          \"id\": \"ref0\"\n" + "        }\n" + "      ],\n"
					+ "      \"script\": \"print \\\"Hello world\\\"\",\n" + "      \"string\": \"str\",\n"
					+ "      \"text\": \"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam consectetur auctor lectus sed tincidunt. Fusce sodales quis mauris non aliquam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Integer maximus imperdiet velit quis vehicula. Mauris pulvinar amet.\",\n"
					+ "      \"xref\": {\n" + "        \"_href\": \"/api/v2/refEntity/ref0\",\n"
					+ "        \"id\": \"ref0\"\n" + "      },\n" + "      \"categorical_mrefOptional\": [],\n"
					+ "      \"mrefOptional\": []\n" + "    }\n" + "  ]\n" + "}";

	private static String resourcePartialSubSubAttributesResponse =
			"{\n" + "  \"_meta\": {\n" + "    \"href\": \"/api/v2/entity\",\n"
					+ "    \"hrefCollection\": \"/api/v2/entity\",\n" + "    \"name\": \"entity\",\n"
					+ "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
					+ "        \"href\": \"/api/v2/entity/meta/xref\",\n" + "        \"fieldType\": \"XREF\",\n"
					+ "        \"name\": \"xref\",\n" + "        \"label\": \"xref\",\n"
					+ "        \"attributes\": [],\n" + "        \"refEntity\": {\n"
					+ "          \"href\": \"/api/v2/refEntity\",\n"
					+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
					+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
					+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n"
					+ "              \"fieldType\": \"STRING\",\n" + "              \"name\": \"id\",\n"
					+ "              \"label\": \"id\",\n" + "              \"attributes\": [],\n"
					+ "              \"maxLength\": 255,\n" + "              \"auto\": false,\n"
					+ "              \"nillable\": false,\n" + "              \"readOnly\": true,\n"
					+ "              \"labelAttribute\": true,\n" + "              \"unique\": true,\n"
					+ "              \"visible\": true,\n" + "              \"lookupAttribute\": true,\n"
					+ "              \"aggregateable\": false\n" + "            },\n" + "            {\n"
					+ "              \"href\": \"/api/v2/refEntity/meta/ref\",\n"
					+ "              \"fieldType\": \"XREF\",\n" + "              \"name\": \"ref\",\n"
					+ "              \"label\": \"ref\",\n" + "              \"attributes\": [],\n"
					+ "              \"refEntity\": {\n" + "                \"href\": \"/api/v2/refRefEntity\",\n"
					+ "                \"hrefCollection\": \"/api/v2/refRefEntity\",\n"
					+ "                \"name\": \"refRefEntity\",\n" + "                \"label\": \"refRefEntity\",\n"
					+ "                \"attributes\": [\n" + "                  {\n"
					+ "                    \"href\": \"/api/v2/refRefEntity/meta/value\",\n"
					+ "                    \"fieldType\": \"STRING\",\n" + "                    \"name\": \"value\",\n"
					+ "                    \"label\": \"value\",\n" + "                    \"attributes\": [],\n"
					+ "                    \"maxLength\": 255,\n" + "                    \"auto\": false,\n"
					+ "                    \"nillable\": true,\n" + "                    \"readOnly\": false,\n"
					+ "                    \"labelAttribute\": false,\n" + "                    \"unique\": false,\n"
					+ "                    \"visible\": true,\n" + "                    \"lookupAttribute\": false,\n"
					+ "                    \"aggregateable\": false\n" + "                  }\n"
					+ "                ],\n" + "                \"labelAttribute\": \"id\",\n"
					+ "                \"idAttribute\": \"id\",\n" + "                \"lookupAttributes\": [\n"
					+ "                  \"id\"\n" + "                ],\n" + "                \"isAbstract\": false,\n"
					+ "                \"writable\": false\n" + "              },\n"
					+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
					+ "              \"readOnly\": false,\n" + "              \"labelAttribute\": false,\n"
					+ "              \"unique\": false,\n" + "              \"visible\": true,\n"
					+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
					+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
					+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [\n"
					+ "            \"id\"\n" + "          ],\n" + "          \"isAbstract\": false,\n"
					+ "          \"writable\": false\n" + "        },\n" + "        \"auto\": false,\n"
					+ "        \"nillable\": false,\n" + "        \"readOnly\": false,\n"
					+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
					+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
					+ "        \"aggregateable\": false\n" + "      }\n" + "    ],\n"
					+ "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n"
					+ "    \"lookupAttributes\": [\n" + "      \"id\"\n" + "    ],\n" + "    \"isAbstract\": false,\n"
					+ "    \"writable\": false\n" + "  },\n" + "  \"_href\": \"/api/v2/entity/0\",\n"
					+ "  \"xref\": {\n" + "    \"_href\": \"/api/v2/refEntity/ref0\",\n" + "    \"id\": \"ref0\",\n"
					+ "    \"ref\": {\n" + "      \"_href\": \"/api/v2/refRefEntity/refRef0\",\n"
					+ "      \"value\": \"value\"\n" + "    }\n" + "  }\n" + "}";
}