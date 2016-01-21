package org.molgenis.data.rest.v2;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.CATEGORICAL_MREF;
import static org.molgenis.MolgenisFieldTypes.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.DATE;
import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.EMAIL;
import static org.molgenis.MolgenisFieldTypes.HTML;
import static org.molgenis.MolgenisFieldTypes.HYPERLINK;
import static org.molgenis.MolgenisFieldTypes.INT;
import static org.molgenis.MolgenisFieldTypes.LONG;
import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.SCRIPT;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.testng.Assert.assertEquals;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.mockito.Matchers;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Fetch;
import org.molgenis.data.IdGenerator;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.rest.service.RestService;
import org.molgenis.data.rest.v2.RestControllerV2Test.RestControllerV2Config;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.validation.ConstraintViolation;
import org.molgenis.data.validation.MolgenisValidationException;
import org.molgenis.fieldtypes.EnumField;
import org.molgenis.file.FileStore;
import org.molgenis.security.core.MolgenisPermissionService;
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
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

@WebAppConfiguration
@ContextConfiguration(classes =
{ RestControllerV2Config.class, GsonConfig.class })
public class RestControllerV2Test extends AbstractTestNGSpringContextTests
{
	private static final String SELF_REF_ENTITY_NAME = "selfRefEntity";
	private static final String ENTITY_NAME = "entity";
	private static final String REF_ENTITY_NAME = "refEntity";
	private static final String REF_REF_ENTITY_NAME = "refRefEntity";
	private static final String ENTITY_ID = "0";
	private static final String REF_ENTITY0_ID = "ref0";
	private static final String REF_ENTITY1_ID = "ref0";
	private static final String REF_REF_ENTITY_ID = "refRef0";
	private static final String HREF_ENTITY_COLLECTION = RestControllerV2.BASE_URI + '/' + ENTITY_NAME;
	private static final String HREF_ENTITY_ID = HREF_ENTITY_COLLECTION + '/' + ENTITY_ID;

	@Autowired
	private RestControllerV2 restControllerV2;

	@Autowired
	private FormattingConversionService conversionService;

	@Autowired
	private GsonHttpMessageConverter gsonHttpMessageConverter;

	@Autowired
	private Gson gson;

	@Autowired
	private DataService dataService;

	private MockMvc mockMvc;
	private String attrBool;
	private String attrString;
	private String attrXref;
	private String attrCompound;
	private String attrCompoundAttr0;
	private String attrCompoundAttrCompound;
	private String attrCompoundAttrCompoundAttr0;
	private String refAttrValue;
	private String refAttrId;
	private String refAttrRef;
	private String refRefAttrValue;

	@BeforeMethod
	public void beforeMethod()
	{
		reset(dataService);
		String refRefAttrId = "id";
		refRefAttrValue = "value";
		DefaultEntityMetaData refRefEntityMetaData = new DefaultEntityMetaData(REF_REF_ENTITY_NAME);
		refRefEntityMetaData.addAttribute(refRefAttrId).setDataType(STRING).setIdAttribute(true);
		refRefEntityMetaData.addAttribute(refRefAttrValue).setDataType(STRING);

		DefaultEntityMetaData selfRefEntityMetaData = new DefaultEntityMetaData(SELF_REF_ENTITY_NAME);
		selfRefEntityMetaData.addAttribute("id").setDataType(STRING).setIdAttribute(true);
		selfRefEntityMetaData.addAttribute("selfRef").setDataType(XREF).setRefEntity(selfRefEntityMetaData);

		Entity selfRefEntity = new DefaultEntity(selfRefEntityMetaData, dataService);
		selfRefEntity.set("id", "0");
		selfRefEntity.set("selfRef", selfRefEntity);

		refAttrId = "id";
		refAttrValue = "value";
		refAttrRef = "ref";
		DefaultEntityMetaData refEntityMetaData = new DefaultEntityMetaData(REF_ENTITY_NAME);
		refEntityMetaData.addAttribute(refAttrId).setDataType(STRING).setIdAttribute(true);
		refEntityMetaData.addAttribute(refAttrValue).setDataType(STRING);
		refEntityMetaData.addAttribute(refAttrRef).setDataType(XREF).setRefEntity(refRefEntityMetaData);

		// required
		String attrId = "id";
		attrBool = "bool";
		String attrCategorical = "categorical";
		String attrCategoricalMref = "categorical_mref";
		attrCompound = "compound";
		attrCompoundAttr0 = "compound_attr0";
		attrCompoundAttrCompound = "compound_attrcompound";
		attrCompoundAttrCompoundAttr0 = "compound_attrcompound_attr0";
		String attrDate = "date";
		String attrDateTime = "date_time";
		String attrDecimal = "decimal";
		String attrEmail = "email";
		String attrEnum = "enum";
		String attrHtml = "html";
		String attrHyperlink = "hyperlink";
		String attrInt = "int";
		String attrLong = "long";
		String attrMref = "mref";
		String attrScript = "script";
		attrString = "string";
		String attrText = "text";
		attrXref = "xref";
		// optional
		String attrBoolOptional = "boolOptional";
		String attrCategoricalOptional = "categoricalOptional";
		String attrCategoricalMrefOptional = "categorical_mrefOptional";
		String attrCompoundAttr0Optional = "compound_attr0Optional";
		String attrCompoundAttrCompoundAttr0Optional = "compound_attrcompound_attr0Optional";
		String attrDateOptional = "dateOptional";
		String attrDateTimeOptional = "date_timeOptional";
		String attrDecimalOptional = "decimalOptional";
		String attrEmailOptional = "emailOptional";
		String attrEnumOptional = "enumOptional";
		String attrHtmlOptional = "htmlOptional";
		String attrHyperlinkOptional = "hyperlinkOptional";
		String attrIntOptional = "intOptional";
		String attrLongOptional = "longOptional";
		String attrMrefOptional = "mrefOptional";
		String attrScriptOptional = "scriptOptional";
		String attrStringOptional = "stringOptional";
		String attrTextOptional = "textOptional";
		String attrXrefOptional = "xrefOptional";

		String enum0 = "enum0";
		String enum1 = "enum1";
		String enum2 = "enum2";

		DefaultEntityMetaData entityMetaData = new DefaultEntityMetaData(ENTITY_NAME);
		// required
		entityMetaData.addAttribute(attrId).setDataType(STRING).setIdAttribute(true);
		entityMetaData.addAttribute(attrBool).setDataType(BOOL);
		entityMetaData.addAttribute(attrCategorical).setDataType(CATEGORICAL).setRefEntity(refEntityMetaData);
		entityMetaData.addAttribute(attrCategoricalMref).setDataType(CATEGORICAL_MREF).setRefEntity(refEntityMetaData);
		DefaultAttributeMetaData compoundAttr = entityMetaData.addAttribute(attrCompound).setDataType(COMPOUND);
		entityMetaData.addAttribute(attrDate).setDataType(DATE);
		entityMetaData.addAttribute(attrDateTime).setDataType(DATETIME);
		entityMetaData.addAttribute(attrDecimal).setDataType(DECIMAL).setReadOnly(true);
		entityMetaData.addAttribute(attrEmail).setDataType(EMAIL);
		entityMetaData.addAttribute(attrEnum).setDataType(new EnumField())
				.setEnumOptions(Arrays.asList(enum0, enum1, enum2));
		entityMetaData.addAttribute(attrHtml).setDataType(HTML);
		entityMetaData.addAttribute(attrHyperlink).setDataType(HYPERLINK);
		entityMetaData.addAttribute(attrInt).setDataType(INT);
		entityMetaData.addAttribute(attrLong).setDataType(LONG);
		entityMetaData.addAttribute(attrMref).setDataType(MREF).setRefEntity(refEntityMetaData);
		;
		entityMetaData.addAttribute(attrScript).setDataType(SCRIPT);
		entityMetaData.addAttribute(attrString).setDataType(STRING);
		entityMetaData.addAttribute(attrText).setDataType(TEXT);
		entityMetaData.addAttribute(attrXref).setDataType(XREF).setRefEntity(refEntityMetaData);
		;
		// optional
		entityMetaData.addAttribute(attrBoolOptional).setDataType(BOOL).setNillable(true);
		entityMetaData.addAttribute(attrCategoricalOptional).setDataType(CATEGORICAL).setRefEntity(refEntityMetaData)
				.setNillable(true);
		entityMetaData.addAttribute(attrCategoricalMrefOptional).setDataType(CATEGORICAL_MREF)
				.setRefEntity(refEntityMetaData).setNillable(true);
		entityMetaData.addAttribute(attrDateOptional).setDataType(DATE).setNillable(true);
		entityMetaData.addAttribute(attrDateTimeOptional).setDataType(DATETIME).setNillable(true);
		entityMetaData.addAttribute(attrDecimalOptional).setDataType(DECIMAL).setNillable(true);
		entityMetaData.addAttribute(attrEmailOptional).setDataType(EMAIL).setNillable(true);
		entityMetaData.addAttribute(attrEnumOptional).setDataType(new EnumField())
				.setEnumOptions(Arrays.asList(enum0, enum1, enum2)).setNillable(true);
		entityMetaData.addAttribute(attrHtmlOptional).setDataType(HTML).setNillable(true);
		entityMetaData.addAttribute(attrHyperlinkOptional).setDataType(HYPERLINK).setNillable(true);
		entityMetaData.addAttribute(attrIntOptional).setDataType(INT).setNillable(true);
		entityMetaData.addAttribute(attrLongOptional).setDataType(LONG).setNillable(true);
		entityMetaData.addAttribute(attrMrefOptional).setDataType(MREF).setRefEntity(refEntityMetaData)
				.setNillable(true);
		entityMetaData.addAttribute(attrScriptOptional).setDataType(SCRIPT).setNillable(true);
		entityMetaData.addAttribute(attrStringOptional).setDataType(STRING).setNillable(true);
		entityMetaData.addAttribute(attrTextOptional).setDataType(TEXT).setNillable(true);
		entityMetaData.addAttribute(attrXrefOptional).setDataType(XREF).setRefEntity(refEntityMetaData)
				.setNillable(true);

		DefaultAttributeMetaData compoundAttrCompoundAttr0 = new DefaultAttributeMetaData(attrCompoundAttrCompoundAttr0)
				.setDataType(STRING);
		DefaultAttributeMetaData compoundAttrCompoundAttr0Optional = new DefaultAttributeMetaData(
				attrCompoundAttrCompoundAttr0Optional).setDataType(STRING).setNillable(true);
		DefaultAttributeMetaData compoundAttrCompound = new DefaultAttributeMetaData(attrCompoundAttrCompound)
				.setDataType(COMPOUND);
		compoundAttrCompound.addAttributePart(compoundAttrCompoundAttr0);
		compoundAttrCompound.addAttributePart(compoundAttrCompoundAttr0Optional);

		DefaultAttributeMetaData compoundAttr0 = new DefaultAttributeMetaData(attrCompoundAttr0).setDataType(STRING);
		DefaultAttributeMetaData compoundAttr0Optional = new DefaultAttributeMetaData(attrCompoundAttr0Optional)
				.setDataType(STRING).setNillable(true);
		compoundAttr.addAttributePart(compoundAttr0);
		compoundAttr.addAttributePart(compoundAttr0Optional);
		compoundAttr.addAttributePart(compoundAttrCompound);

		DefaultEntity refRefEntity = new DefaultEntity(refRefEntityMetaData, dataService);
		refRefEntity.set(refRefAttrId, REF_REF_ENTITY_ID);
		refRefEntity.set(refRefAttrValue, "value");

		DefaultEntity refEntity0 = new DefaultEntity(refEntityMetaData, dataService);
		refEntity0.set(refAttrId, REF_ENTITY0_ID);
		refEntity0.set(refAttrValue, "val0");
		refEntity0.set(refAttrRef, refRefEntity);

		DefaultEntity refEntity1 = new DefaultEntity(refEntityMetaData, dataService);
		refEntity1.set(refAttrId, REF_ENTITY1_ID);
		refEntity1.set(refAttrValue, "val1");
		refEntity1.set(refAttrRef, refRefEntity);

		DefaultEntity entity = new DefaultEntity(entityMetaData, dataService);
		// required
		entity.set(attrId, ENTITY_ID);
		entity.set(attrBool, true);
		entity.set(attrCategorical, refEntity0);
		entity.set(attrCategoricalMref, Arrays.asList(refEntity0, refEntity1));
		entity.set(attrCompoundAttr0, "compoundAttr0Str");
		entity.set(attrCompoundAttrCompoundAttr0, "compoundAttrCompoundAttr0Str");
		entity.set(attrDate, "2015-05-22");
		entity.set(attrDateTime, "2015-05-22T11:12:13+0500");
		entity.set(attrDecimal, 3.14);
		entity.set(attrEmail, "my@mail.com");
		entity.set(attrEnum, enum0);
		entity.set(attrHtml, "<h1>html</h1>");
		entity.set(attrHyperlink, "http://www.molgenis.org/");
		entity.set(attrInt, 123);
		entity.set(attrLong, Long.MAX_VALUE);
		entity.set(attrMref, Arrays.asList(refEntity0, refEntity1));
		entity.set(attrScript, "print \"Hello world\"");
		entity.set(attrString, "str");
		String textValue = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam consectetur auctor lectus sed tincidunt. Fusce sodales quis mauris non aliquam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Integer maximus imperdiet velit quis vehicula. Mauris pulvinar amet.";
		entity.set(attrText, textValue);
		entity.set(attrXref, refEntity0);
		// optional
		entity.set(attrBoolOptional, null);
		entity.set(attrCategoricalOptional, null);
		entity.set(attrCategoricalMrefOptional, null);
		entity.set(attrCompoundAttr0Optional, null);
		entity.set(attrCompoundAttrCompoundAttr0Optional, null);
		entity.set(attrDateOptional, null);
		entity.set(attrDateTimeOptional, null);
		entity.set(attrDecimalOptional, null);
		entity.set(attrEmailOptional, null);
		entity.set(attrEnumOptional, null);
		entity.set(attrHtmlOptional, null);
		entity.set(attrHyperlinkOptional, null);
		entity.set(attrIntOptional, null);
		entity.set(attrLongOptional, null);
		entity.set(attrMrefOptional, null);
		entity.set(attrScriptOptional, null);
		entity.set(attrStringOptional, null);
		entity.set(attrTextOptional, null);
		entity.set(attrXrefOptional, null);

		Query q = new QueryImpl().offset(0).pageSize(100);
		when(dataService.findOne(ENTITY_NAME, ENTITY_ID)).thenReturn(entity);
		when(dataService.findOne(eq(ENTITY_NAME), eq(ENTITY_ID), any(Fetch.class))).thenReturn(entity);
		when(dataService.findOne(eq(SELF_REF_ENTITY_NAME), eq("0"), any(Fetch.class))).thenReturn(selfRefEntity);
		when(dataService.count(ENTITY_NAME, q)).thenReturn(2l);
		when(dataService.findAll(ENTITY_NAME, q)).thenReturn(Stream.of(entity));
		when(dataService.findOne(REF_ENTITY_NAME, REF_ENTITY0_ID)).thenReturn(refEntity0);
		when(dataService.findOne(REF_ENTITY_NAME, REF_ENTITY1_ID)).thenReturn(refEntity1);
		when(dataService.findOne(REF_REF_ENTITY_NAME, REF_REF_ENTITY_ID)).thenReturn(refRefEntity);
		when(dataService.getEntityMetaData(ENTITY_NAME)).thenReturn(entityMetaData);
		when(dataService.getEntityMetaData(REF_ENTITY_NAME)).thenReturn(refEntityMetaData);
		when(dataService.getEntityMetaData(REF_REF_ENTITY_NAME)).thenReturn(refRefEntityMetaData);
		when(dataService.getEntityMetaData(SELF_REF_ENTITY_NAME)).thenReturn(selfRefEntityMetaData);

		Assert.assertEquals(entity.getIdValue(), ENTITY_ID);
		Assert.assertEquals(refEntity0.getIdValue(), REF_ENTITY0_ID);
		Assert.assertEquals(refEntity1.getIdValue(), REF_ENTITY1_ID);
		Assert.assertEquals(refRefEntity.getIdValue(), REF_REF_ENTITY_ID);
		Assert.assertEquals(selfRefEntity.getIdValue(), "0");

		mockMvc = MockMvcBuilders.standaloneSetup(restControllerV2).setMessageConverters(gsonHttpMessageConverter)
				.setConversionService(conversionService).build();
	}

	@Test
	public void retrieveAtrributeMetaData()
	{
		Assert.assertEquals(restControllerV2.retrieveEntityAttributeMeta(ENTITY_NAME, "id").getHref(),
				"/api/v2/entity/meta/id");
		Assert.assertEquals(restControllerV2.retrieveEntityAttributeMeta(ENTITY_NAME, "id").getName(), "id");
		Assert.assertEquals(restControllerV2.retrieveEntityAttributeMeta(ENTITY_NAME, "id").getDescription(), null);
	}

	@Test
	public void retrieveAtrributeMetaDataPost()
	{
		Assert.assertEquals(restControllerV2.retrieveEntityAttributeMetaPost(ENTITY_NAME, "id").getHref(),
				"/api/v2/entity/meta/id");
		Assert.assertEquals(restControllerV2.retrieveEntityAttributeMetaPost(ENTITY_NAME, "id").getName(), "id");
		Assert.assertEquals(restControllerV2.retrieveEntityAttributeMetaPost(ENTITY_NAME, "id").getDescription(), null);
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
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs", attrBool)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(resourcePartialAttributeResponse));
	}

	@Test
	public void retrieveResourcePartialResponseAttributeInCompound() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs", attrCompound + '(' + attrCompoundAttr0 + ')'))
				.andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(resourcePartialAttributeInCompoundResponse));
	}

	@Test
	public void retrieveResourcePartialResponseAttributeInCompoundInCompound() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs",
				attrCompound + '(' + attrCompoundAttrCompound + '(' + attrCompoundAttrCompoundAttr0 + "))"))
				.andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(resourcePartialAttributeInCompoundInCompoundResponse));
	}

	@Test
	public void retrieveResourcePartialResponseAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs", attrBool + ',' + attrString)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(resourcePartialAttributesResponse));
	}

	@Test
	public void retrieveResourcePartialResponseSubAttribute() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs", attrXref + '(' + refAttrValue + ')'))
				.andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(resourcePartialSubAttributeResponse));
	}

	@Test
	public void retrieveResourcePartialResponseSubAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs", attrXref + '(' + refAttrId + ',' + refAttrValue + ')'))
				.andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(resourcePartialSubAttributesResponse));
	}

	@Test
	public void retrieveResourcePartialResponseSubSubAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attrs",
				attrXref + '(' + refAttrId + ',' + refAttrRef + '(' + refRefAttrValue + ')' + ')'))
				.andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
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
		String responseBody = "{\n  \"location\": \"/api/v2/entity?q=id=in=(\\\"p1\\\",\\\"p2\\\")\",\n  \"resources\": [\n    {\n      \"href\": \"/api/v2/entity/p1\"\n    },\n"
				+ "    {\n      \"href\": \"/api/v2/entity/p2\"\n    }\n  ]\n}";
		mockMvc.perform(post(HREF_ENTITY_COLLECTION).content(content).contentType(APPLICATION_JSON))
				.andExpect(status().isCreated()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string(responseBody));

		verify(dataService).add(eq(ENTITY_NAME), any(Stream.class));
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
		doThrow(e).when(dataService).add(eq(ENTITY_NAME), any(Stream.class));

		String content = "{entities:[{id:'p1', name:'Example data'}]}";
		ResultActions resultActions = mockMvc
				.perform(post(HREF_ENTITY_COLLECTION).content(content).contentType(APPLICATION_JSON))
				.andExpect(status().isInternalServerError()).andExpect(content().contentType(APPLICATION_JSON))
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

		verify(dataService, times(1)).update(eq(ENTITY_NAME), any(Stream.class));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateEntitiesMolgenisDataException() throws Exception
	{
		Exception e = new MolgenisDataException("Check if this exception is not swallowed by the system");
		doThrow(e).when(dataService).update(Matchers.eq(ENTITY_NAME), any(Stream.class));

		String content = "{entities:[{id:'p1', name:'Example data'}]}";
		ResultActions resultActions = mockMvc
				.perform(put(HREF_ENTITY_COLLECTION).content(content).contentType(APPLICATION_JSON))
				.andExpect(status().isInternalServerError()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(header().doesNotExist("Location"));

		this.assertEqualsErrorMessage(resultActions, e.getMessage());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testUpdateEntitiesMolgenisValidationException() throws Exception
	{
		Exception e = new MolgenisValidationException(
				Collections.singleton(new ConstraintViolation("Message", Long.valueOf(5L))));
		doThrow(e).when(dataService).update(eq(ENTITY_NAME), any(Stream.class));

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

		verify(dataService, times(1)).update(eq(ENTITY_NAME), any(Stream.class));

		Entity entity = dataService.findOne(ENTITY_NAME, ENTITY_ID);
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
		Map<String, Object> lvl1 = gson.fromJson(responseWithAttrs.getContentAsString(),
				new TypeToken<Map<String, Object>>()
				{
				}.getType());
		assertEquals(lvl1.get("selfRef").toString(), "{_href=/api/v2/selfRefEntity/0, id=0}");
	}

	@Test
	public void testSelfRefWithNestedFetch() throws Exception
	{
		MockHttpServletResponse responseWithAttrs = mockMvc
				.perform(get(RestControllerV2.BASE_URI + "/selfRefEntity/0?attrs=*,selfRef(*,selfRef(*))")
						.contentType(APPLICATION_JSON))
				.andReturn().getResponse();
		assertEquals(responseWithAttrs.getStatus(), 200);
		Map<String, Object> lvl1 = gson.fromJson(responseWithAttrs.getContentAsString(),
				new TypeToken<Map<String, Object>>()
				{
				}.getType());
		@SuppressWarnings("unchecked")
		Map<String, Object> lvl2 = (Map<String, Object>) lvl1.get("selfRef");
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
		ResultActions resultActions = mockMvc
				.perform(put(RestControllerV2.BASE_URI + "/" + entityName + "/" + attributeName).content(content)
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
		public RestControllerV2 restController()
		{
			return new RestControllerV2(dataService(), molgenisPermissionService(),
					new RestService(dataService(), idGenerator(), fileStore()), languageService());
		}

	}

	private final String resourceResponse = "{\n" + "  \"_meta\": {\n" + "    \"href\": \"/api/v2/entity\",\n"
			+ "    \"hrefCollection\": \"/api/v2/entity\",\n" + "    \"name\": \"entity\",\n"
			+ "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/id\",\n" + "        \"fieldType\": \"STRING\",\n"
			+ "        \"name\": \"id\",\n" + "        \"label\": \"id\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": true,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": true,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/bool\",\n" + "        \"fieldType\": \"BOOL\",\n"
			+ "        \"name\": \"bool\",\n" + "        \"label\": \"bool\",\n" + "        \"attributes\": [],\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
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
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/categorical_mref\",\n"
			+ "        \"fieldType\": \"CATEGORICAL_MREF\",\n" + "        \"name\": \"categorical_mref\",\n"
			+ "        \"label\": \"categorical_mref\",\n" + "        \"attributes\": [],\n"
			+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
			+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
			+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/compound\",\n"
			+ "        \"fieldType\": \"COMPOUND\",\n" + "        \"name\": \"compound\",\n"
			+ "        \"label\": \"compound\",\n" + "        \"attributes\": [\n" + "          {\n"
			+ "            \"href\": \"/api/v2/entity/meta/compound_attr0\",\n"
			+ "            \"fieldType\": \"STRING\",\n" + "            \"name\": \"compound_attr0\",\n"
			+ "            \"label\": \"compound_attr0\",\n" + "            \"attributes\": [],\n"
			+ "            \"maxLength\": 255,\n" + "            \"auto\": false,\n"
			+ "            \"nillable\": true,\n" + "            \"readOnly\": false,\n"
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
			+ "                \"nillable\": true,\n" + "                \"readOnly\": false,\n"
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
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/date_time\",\n"
			+ "        \"fieldType\": \"DATE_TIME\",\n" + "        \"name\": \"date_time\",\n"
			+ "        \"label\": \"date_time\",\n" + "        \"attributes\": [],\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/decimal\",\n"
			+ "        \"fieldType\": \"DECIMAL\",\n" + "        \"name\": \"decimal\",\n"
			+ "        \"label\": \"decimal\",\n" + "        \"attributes\": [],\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": true,\n" + "        \"labelAttribute\": false,\n"
			+ "        \"unique\": false,\n" + "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/email\",\n" + "        \"fieldType\": \"EMAIL\",\n"
			+ "        \"name\": \"email\",\n" + "        \"label\": \"email\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/enum\",\n" + "        \"fieldType\": \"ENUM\",\n"
			+ "        \"name\": \"enum\",\n" + "        \"label\": \"enum\",\n" + "        \"attributes\": [],\n"
			+ "        \"enumOptions\": [\n" + "          \"enum0\",\n" + "          \"enum1\",\n"
			+ "          \"enum2\"\n" + "        ],\n" + "        \"maxLength\": 255,\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/html\",\n" + "        \"fieldType\": \"HTML\",\n"
			+ "        \"name\": \"html\",\n" + "        \"label\": \"html\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/hyperlink\",\n" + "        \"fieldType\": \"HYPERLINK\",\n"
			+ "        \"name\": \"hyperlink\",\n" + "        \"label\": \"hyperlink\",\n"
			+ "        \"attributes\": [],\n" + "        \"maxLength\": 255,\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/int\",\n" + "        \"fieldType\": \"INT\",\n"
			+ "        \"name\": \"int\",\n" + "        \"label\": \"int\",\n" + "        \"attributes\": [],\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/long\",\n" + "        \"fieldType\": \"LONG\",\n"
			+ "        \"name\": \"long\",\n" + "        \"label\": \"long\",\n" + "        \"attributes\": [],\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
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
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/script\",\n"
			+ "        \"fieldType\": \"SCRIPT\",\n" + "        \"name\": \"script\",\n"
			+ "        \"label\": \"script\",\n" + "        \"attributes\": [],\n" + "        \"maxLength\": 65535,\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/string\",\n"
			+ "        \"fieldType\": \"STRING\",\n" + "        \"name\": \"string\",\n"
			+ "        \"label\": \"string\",\n" + "        \"attributes\": [],\n" + "        \"maxLength\": 255,\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/text\",\n" + "        \"fieldType\": \"TEXT\",\n"
			+ "        \"name\": \"text\",\n" + "        \"label\": \"text\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
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
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/boolOptional\",\n"
			+ "        \"fieldType\": \"BOOL\",\n" + "        \"name\": \"boolOptional\",\n"
			+ "        \"label\": \"boolOptional\",\n" + "        \"attributes\": [],\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/categoricalOptional\",\n"
			+ "        \"fieldType\": \"CATEGORICAL\",\n" + "        \"name\": \"categoricalOptional\",\n"
			+ "        \"label\": \"categoricalOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
			+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
			+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/categorical_mrefOptional\",\n"
			+ "        \"fieldType\": \"CATEGORICAL_MREF\",\n" + "        \"name\": \"categorical_mrefOptional\",\n"
			+ "        \"label\": \"categorical_mrefOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
			+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
			+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/dateOptional\",\n"
			+ "        \"fieldType\": \"DATE\",\n" + "        \"name\": \"dateOptional\",\n"
			+ "        \"label\": \"dateOptional\",\n" + "        \"attributes\": [],\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/date_timeOptional\",\n"
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
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/scriptOptional\",\n"
			+ "        \"fieldType\": \"SCRIPT\",\n" + "        \"name\": \"scriptOptional\",\n"
			+ "        \"label\": \"scriptOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/stringOptional\",\n" + "        \"fieldType\": \"STRING\",\n"
			+ "        \"name\": \"stringOptional\",\n" + "        \"label\": \"stringOptional\",\n"
			+ "        \"attributes\": [],\n" + "        \"maxLength\": 255,\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/textOptional\",\n"
			+ "        \"fieldType\": \"TEXT\",\n" + "        \"name\": \"textOptional\",\n"
			+ "        \"label\": \"textOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/xrefOptional\",\n" + "        \"fieldType\": \"XREF\",\n"
			+ "        \"name\": \"xrefOptional\",\n" + "        \"label\": \"xrefOptional\",\n"
			+ "        \"attributes\": [],\n" + "        \"refEntity\": {\n"
			+ "          \"href\": \"/api/v2/refEntity\",\n" + "          \"hrefCollection\": \"/api/v2/refEntity\",\n"
			+ "          \"name\": \"refEntity\",\n" + "          \"label\": \"refEntity\",\n"
			+ "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      }\n" + "    ],\n"
			+ "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n" + "    \"lookupAttributes\": [],\n"
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

	private final String resourcePartialAttributeResponse = "{\n" + "  \"_meta\": {\n"
			+ "    \"href\": \"/api/v2/entity\",\n" + "    \"hrefCollection\": \"/api/v2/entity\",\n"
			+ "    \"name\": \"entity\",\n" + "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/bool\",\n" + "        \"fieldType\": \"BOOL\",\n"
			+ "        \"name\": \"bool\",\n" + "        \"label\": \"bool\",\n" + "        \"attributes\": [],\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      }\n" + "    ],\n"
			+ "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n" + "    \"lookupAttributes\": [],\n"
			+ "    \"isAbstract\": false,\n" + "    \"writable\": false\n" + "  },\n"
			+ "  \"_href\": \"/api/v2/entity/0\",\n" + "  \"bool\": true\n" + "}";

	private final String resourcePartialAttributeInCompoundResponse = "{\n" + "  \"_meta\": {\n"
			+ "    \"href\": \"/api/v2/entity\",\n" + "    \"hrefCollection\": \"/api/v2/entity\",\n"
			+ "    \"name\": \"entity\",\n" + "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/compound\",\n" + "        \"fieldType\": \"COMPOUND\",\n"
			+ "        \"name\": \"compound\",\n" + "        \"label\": \"compound\",\n" + "        \"attributes\": [\n"
			+ "          {\n" + "            \"href\": \"/api/v2/entity/meta/compound_attr0\",\n"
			+ "            \"fieldType\": \"STRING\",\n" + "            \"name\": \"compound_attr0\",\n"
			+ "            \"label\": \"compound_attr0\",\n" + "            \"attributes\": [],\n"
			+ "            \"maxLength\": 255,\n" + "            \"auto\": false,\n"
			+ "            \"nillable\": true,\n" + "            \"readOnly\": false,\n"
			+ "            \"labelAttribute\": false,\n" + "            \"unique\": false,\n"
			+ "            \"visible\": true,\n" + "            \"lookupAttribute\": false,\n"
			+ "            \"aggregateable\": false\n" + "          }\n" + "        ],\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      }\n" + "    ],\n"
			+ "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n" + "    \"lookupAttributes\": [],\n"
			+ "    \"isAbstract\": false,\n" + "    \"writable\": false\n" + "  },\n"
			+ "  \"_href\": \"/api/v2/entity/0\",\n" + "  \"compound_attr0\": \"compoundAttr0Str\"\n" + "}";

	private final String resourcePartialAttributeInCompoundInCompoundResponse = "{\n" + "  \"_meta\": {\n"
			+ "    \"href\": \"/api/v2/entity\",\n" + "    \"hrefCollection\": \"/api/v2/entity\",\n"
			+ "    \"name\": \"entity\",\n" + "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/compound\",\n" + "        \"fieldType\": \"COMPOUND\",\n"
			+ "        \"name\": \"compound\",\n" + "        \"label\": \"compound\",\n" + "        \"attributes\": [\n"
			+ "          {\n" + "            \"href\": \"/api/v2/entity/meta/compound_attrcompound\",\n"
			+ "            \"fieldType\": \"COMPOUND\",\n" + "            \"name\": \"compound_attrcompound\",\n"
			+ "            \"label\": \"compound_attrcompound\",\n" + "            \"attributes\": [\n"
			+ "              {\n" + "                \"href\": \"/api/v2/entity/meta/compound_attrcompound_attr0\",\n"
			+ "                \"fieldType\": \"STRING\",\n"
			+ "                \"name\": \"compound_attrcompound_attr0\",\n"
			+ "                \"label\": \"compound_attrcompound_attr0\",\n" + "                \"attributes\": [],\n"
			+ "                \"maxLength\": 255,\n" + "                \"auto\": false,\n"
			+ "                \"nillable\": true,\n" + "                \"readOnly\": false,\n"
			+ "                \"labelAttribute\": false,\n" + "                \"unique\": false,\n"
			+ "                \"visible\": true,\n" + "                \"lookupAttribute\": false,\n"
			+ "                \"aggregateable\": false\n" + "              }\n" + "            ],\n"
			+ "            \"auto\": false,\n" + "            \"nillable\": true,\n"
			+ "            \"readOnly\": false,\n" + "            \"labelAttribute\": false,\n"
			+ "            \"unique\": false,\n" + "            \"visible\": true,\n"
			+ "            \"lookupAttribute\": false,\n" + "            \"aggregateable\": false\n" + "          }\n"
			+ "        ],\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      }\n" + "    ],\n" + "    \"labelAttribute\": \"id\",\n"
			+ "    \"idAttribute\": \"id\",\n" + "    \"lookupAttributes\": [],\n" + "    \"isAbstract\": false,\n"
			+ "    \"writable\": false\n" + "  },\n" + "  \"_href\": \"/api/v2/entity/0\",\n"
			+ "  \"compound_attrcompound_attr0\": \"compoundAttrCompoundAttr0Str\"\n" + "}";

	private final String resourcePartialSubAttributeResponse = "{\n" + "  \"_meta\": {\n"
			+ "    \"href\": \"/api/v2/entity\",\n" + "    \"hrefCollection\": \"/api/v2/entity\",\n"
			+ "    \"name\": \"entity\",\n" + "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/xref\",\n" + "        \"fieldType\": \"XREF\",\n"
			+ "        \"name\": \"xref\",\n" + "        \"label\": \"xref\",\n" + "        \"attributes\": [],\n"
			+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
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
			+ "          \"lookupAttributes\": [],\n" + "          \"isAbstract\": false,\n"
			+ "          \"writable\": false\n" + "        },\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      }\n" + "    ],\n"
			+ "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n" + "    \"lookupAttributes\": [],\n"
			+ "    \"isAbstract\": false,\n" + "    \"writable\": false\n" + "  },\n"
			+ "  \"_href\": \"/api/v2/entity/0\",\n" + "  \"xref\": {\n"
			+ "    \"_href\": \"/api/v2/refEntity/ref0\",\n" + "    \"value\": \"val0\"\n" + "  }\n" + "}";

	private final String resourcePartialSubAttributesResponse = "{\n" + "  \"_meta\": {\n"
			+ "    \"href\": \"/api/v2/entity\",\n" + "    \"hrefCollection\": \"/api/v2/entity\",\n"
			+ "    \"name\": \"entity\",\n" + "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/xref\",\n" + "        \"fieldType\": \"XREF\",\n"
			+ "        \"name\": \"xref\",\n" + "        \"label\": \"xref\",\n" + "        \"attributes\": [],\n"
			+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
			+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
			+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            },\n" + "            {\n" + "              \"href\": \"/api/v2/refEntity/meta/value\",\n"
			+ "              \"fieldType\": \"STRING\",\n" + "              \"name\": \"value\",\n"
			+ "              \"label\": \"value\",\n" + "              \"attributes\": [],\n"
			+ "              \"maxLength\": 255,\n" + "              \"auto\": false,\n"
			+ "              \"nillable\": true,\n" + "              \"readOnly\": false,\n"
			+ "              \"labelAttribute\": false,\n" + "              \"unique\": false,\n"
			+ "              \"visible\": true,\n" + "              \"lookupAttribute\": false,\n"
			+ "              \"aggregateable\": false\n" + "            }\n" + "          ],\n"
			+ "          \"labelAttribute\": \"id\",\n" + "          \"idAttribute\": \"id\",\n"
			+ "          \"lookupAttributes\": [],\n" + "          \"isAbstract\": false,\n"
			+ "          \"writable\": false\n" + "        },\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      }\n" + "    ],\n"
			+ "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n" + "    \"lookupAttributes\": [],\n"
			+ "    \"isAbstract\": false,\n" + "    \"writable\": false\n" + "  },\n"
			+ "  \"_href\": \"/api/v2/entity/0\",\n" + "  \"xref\": {\n"
			+ "    \"_href\": \"/api/v2/refEntity/ref0\",\n" + "    \"id\": \"ref0\",\n" + "    \"value\": \"val0\"\n"
			+ "  }\n" + "}";

	private final String resourcePartialAttributesResponse = "{\n" + "  \"_meta\": {\n"
			+ "    \"href\": \"/api/v2/entity\",\n" + "    \"hrefCollection\": \"/api/v2/entity\",\n"
			+ "    \"name\": \"entity\",\n" + "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/bool\",\n" + "        \"fieldType\": \"BOOL\",\n"
			+ "        \"name\": \"bool\",\n" + "        \"label\": \"bool\",\n" + "        \"attributes\": [],\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/string\",\n"
			+ "        \"fieldType\": \"STRING\",\n" + "        \"name\": \"string\",\n"
			+ "        \"label\": \"string\",\n" + "        \"attributes\": [],\n" + "        \"maxLength\": 255,\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      }\n" + "    ],\n"
			+ "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n" + "    \"lookupAttributes\": [],\n"
			+ "    \"isAbstract\": false,\n" + "    \"writable\": false\n" + "  },\n"
			+ "  \"_href\": \"/api/v2/entity/0\",\n" + "  \"bool\": true,\n" + "  \"string\": \"str\"\n" + "}";

	private final String resourceCollectionResponse = "{\n" + "  \"href\": \"/api/v2/entity\",\n" + "  \"meta\": {\n"
			+ "    \"href\": \"/api/v2/entity\",\n" + "    \"hrefCollection\": \"/api/v2/entity\",\n"
			+ "    \"name\": \"entity\",\n" + "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/id\",\n" + "        \"fieldType\": \"STRING\",\n"
			+ "        \"name\": \"id\",\n" + "        \"label\": \"id\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": true,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": true,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/bool\",\n" + "        \"fieldType\": \"BOOL\",\n"
			+ "        \"name\": \"bool\",\n" + "        \"label\": \"bool\",\n" + "        \"attributes\": [],\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
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
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/categorical_mref\",\n"
			+ "        \"fieldType\": \"CATEGORICAL_MREF\",\n" + "        \"name\": \"categorical_mref\",\n"
			+ "        \"label\": \"categorical_mref\",\n" + "        \"attributes\": [],\n"
			+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
			+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
			+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/compound\",\n"
			+ "        \"fieldType\": \"COMPOUND\",\n" + "        \"name\": \"compound\",\n"
			+ "        \"label\": \"compound\",\n" + "        \"attributes\": [\n" + "          {\n"
			+ "            \"href\": \"/api/v2/entity/meta/compound_attr0\",\n"
			+ "            \"fieldType\": \"STRING\",\n" + "            \"name\": \"compound_attr0\",\n"
			+ "            \"label\": \"compound_attr0\",\n" + "            \"attributes\": [],\n"
			+ "            \"maxLength\": 255,\n" + "            \"auto\": false,\n"
			+ "            \"nillable\": true,\n" + "            \"readOnly\": false,\n"
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
			+ "                \"nillable\": true,\n" + "                \"readOnly\": false,\n"
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
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/date_time\",\n"
			+ "        \"fieldType\": \"DATE_TIME\",\n" + "        \"name\": \"date_time\",\n"
			+ "        \"label\": \"date_time\",\n" + "        \"attributes\": [],\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/decimal\",\n"
			+ "        \"fieldType\": \"DECIMAL\",\n" + "        \"name\": \"decimal\",\n"
			+ "        \"label\": \"decimal\",\n" + "        \"attributes\": [],\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": true,\n" + "        \"labelAttribute\": false,\n"
			+ "        \"unique\": false,\n" + "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/email\",\n" + "        \"fieldType\": \"EMAIL\",\n"
			+ "        \"name\": \"email\",\n" + "        \"label\": \"email\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 255,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/enum\",\n" + "        \"fieldType\": \"ENUM\",\n"
			+ "        \"name\": \"enum\",\n" + "        \"label\": \"enum\",\n" + "        \"attributes\": [],\n"
			+ "        \"enumOptions\": [\n" + "          \"enum0\",\n" + "          \"enum1\",\n"
			+ "          \"enum2\"\n" + "        ],\n" + "        \"maxLength\": 255,\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/html\",\n" + "        \"fieldType\": \"HTML\",\n"
			+ "        \"name\": \"html\",\n" + "        \"label\": \"html\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/hyperlink\",\n" + "        \"fieldType\": \"HYPERLINK\",\n"
			+ "        \"name\": \"hyperlink\",\n" + "        \"label\": \"hyperlink\",\n"
			+ "        \"attributes\": [],\n" + "        \"maxLength\": 255,\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/int\",\n" + "        \"fieldType\": \"INT\",\n"
			+ "        \"name\": \"int\",\n" + "        \"label\": \"int\",\n" + "        \"attributes\": [],\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/long\",\n" + "        \"fieldType\": \"LONG\",\n"
			+ "        \"name\": \"long\",\n" + "        \"label\": \"long\",\n" + "        \"attributes\": [],\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
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
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/script\",\n"
			+ "        \"fieldType\": \"SCRIPT\",\n" + "        \"name\": \"script\",\n"
			+ "        \"label\": \"script\",\n" + "        \"attributes\": [],\n" + "        \"maxLength\": 65535,\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/string\",\n"
			+ "        \"fieldType\": \"STRING\",\n" + "        \"name\": \"string\",\n"
			+ "        \"label\": \"string\",\n" + "        \"attributes\": [],\n" + "        \"maxLength\": 255,\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/text\",\n" + "        \"fieldType\": \"TEXT\",\n"
			+ "        \"name\": \"text\",\n" + "        \"label\": \"text\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
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
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/boolOptional\",\n"
			+ "        \"fieldType\": \"BOOL\",\n" + "        \"name\": \"boolOptional\",\n"
			+ "        \"label\": \"boolOptional\",\n" + "        \"attributes\": [],\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/categoricalOptional\",\n"
			+ "        \"fieldType\": \"CATEGORICAL\",\n" + "        \"name\": \"categoricalOptional\",\n"
			+ "        \"label\": \"categoricalOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
			+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
			+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/categorical_mrefOptional\",\n"
			+ "        \"fieldType\": \"CATEGORICAL_MREF\",\n" + "        \"name\": \"categorical_mrefOptional\",\n"
			+ "        \"label\": \"categorical_mrefOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
			+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
			+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/dateOptional\",\n"
			+ "        \"fieldType\": \"DATE\",\n" + "        \"name\": \"dateOptional\",\n"
			+ "        \"label\": \"dateOptional\",\n" + "        \"attributes\": [],\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/date_timeOptional\",\n"
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
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/scriptOptional\",\n"
			+ "        \"fieldType\": \"SCRIPT\",\n" + "        \"name\": \"scriptOptional\",\n"
			+ "        \"label\": \"scriptOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/stringOptional\",\n" + "        \"fieldType\": \"STRING\",\n"
			+ "        \"name\": \"stringOptional\",\n" + "        \"label\": \"stringOptional\",\n"
			+ "        \"attributes\": [],\n" + "        \"maxLength\": 255,\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      },\n"
			+ "      {\n" + "        \"href\": \"/api/v2/entity/meta/textOptional\",\n"
			+ "        \"fieldType\": \"TEXT\",\n" + "        \"name\": \"textOptional\",\n"
			+ "        \"label\": \"textOptional\",\n" + "        \"attributes\": [],\n"
			+ "        \"maxLength\": 65535,\n" + "        \"auto\": false,\n" + "        \"nillable\": true,\n"
			+ "        \"readOnly\": false,\n" + "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n"
			+ "        \"visible\": true,\n" + "        \"lookupAttribute\": false,\n"
			+ "        \"aggregateable\": false\n" + "      },\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/xrefOptional\",\n" + "        \"fieldType\": \"XREF\",\n"
			+ "        \"name\": \"xrefOptional\",\n" + "        \"label\": \"xrefOptional\",\n"
			+ "        \"attributes\": [],\n" + "        \"refEntity\": {\n"
			+ "          \"href\": \"/api/v2/refEntity\",\n" + "          \"hrefCollection\": \"/api/v2/refEntity\",\n"
			+ "          \"name\": \"refEntity\",\n" + "          \"label\": \"refEntity\",\n"
			+ "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            }\n" + "          ],\n" + "          \"labelAttribute\": \"id\",\n"
			+ "          \"idAttribute\": \"id\",\n" + "          \"lookupAttributes\": [],\n"
			+ "          \"isAbstract\": false,\n" + "          \"writable\": false\n" + "        },\n"
			+ "        \"auto\": false,\n" + "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      }\n" + "    ],\n"
			+ "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n" + "    \"lookupAttributes\": [],\n"
			+ "    \"isAbstract\": false,\n" + "    \"writable\": false\n" + "  },\n" + "  \"start\": 0,\n"
			+ "  \"num\": 100,\n" + "  \"total\": 2,\n" + "  \"items\": [\n" + "    {\n"
			+ "      \"_href\": \"/api/v2/entity/0\",\n" + "      \"id\": \"0\",\n" + "      \"bool\": true,\n"
			+ "      \"categorical\": {\n" + "        \"_href\": \"/api/v2/refEntity/ref0\",\n"
			+ "        \"id\": \"ref0\"\n" + "      },\n" + "      \"categorical_mref\": [\n" + "        {\n"
			+ "          \"_href\": \"/api/v2/refEntity/ref0\",\n" + "          \"id\": \"ref0\"\n" + "        },\n"
			+ "        {\n" + "          \"_href\": \"/api/v2/refEntity/ref0\",\n" + "          \"id\": \"ref0\"\n"
			+ "        }\n" + "      ],\n" + "      \"compound_attr0\": \"compoundAttr0Str\",\n"
			+ "      \"compound_attrcompound_attr0\": \"compoundAttrCompoundAttr0Str\",\n"
			+ "      \"date\": \"2015-05-22\",\n" + "      \"date_time\": \"2015-05-22T08:12:13+0200\",\n"
			+ "      \"decimal\": 3.14,\n" + "      \"email\": \"my@mail.com\",\n" + "      \"enum\": \"enum0\",\n"
			+ "      \"html\": \"<h1>html</h1>\",\n" + "      \"hyperlink\": \"http://www.molgenis.org/\",\n"
			+ "      \"int\": 123,\n" + "      \"long\": 9223372036854775807,\n" + "      \"mref\": [\n" + "        {\n"
			+ "          \"_href\": \"/api/v2/refEntity/ref0\",\n" + "          \"id\": \"ref0\"\n" + "        },\n"
			+ "        {\n" + "          \"_href\": \"/api/v2/refEntity/ref0\",\n" + "          \"id\": \"ref0\"\n"
			+ "        }\n" + "      ],\n" + "      \"script\": \"print \\\"Hello world\\\"\",\n"
			+ "      \"string\": \"str\",\n"
			+ "      \"text\": \"Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam consectetur auctor lectus sed tincidunt. Fusce sodales quis mauris non aliquam. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia Curae; Integer maximus imperdiet velit quis vehicula. Mauris pulvinar amet.\",\n"
			+ "      \"xref\": {\n" + "        \"_href\": \"/api/v2/refEntity/ref0\",\n" + "        \"id\": \"ref0\"\n"
			+ "      },\n" + "      \"categorical_mrefOptional\": [],\n" + "      \"mrefOptional\": []\n" + "    }\n"
			+ "  ]\n" + "}";

	private static String resourcePartialSubSubAttributesResponse = "{\n" + "  \"_meta\": {\n"
			+ "    \"href\": \"/api/v2/entity\",\n" + "    \"hrefCollection\": \"/api/v2/entity\",\n"
			+ "    \"name\": \"entity\",\n" + "    \"label\": \"entity\",\n" + "    \"attributes\": [\n" + "      {\n"
			+ "        \"href\": \"/api/v2/entity/meta/xref\",\n" + "        \"fieldType\": \"XREF\",\n"
			+ "        \"name\": \"xref\",\n" + "        \"label\": \"xref\",\n" + "        \"attributes\": [],\n"
			+ "        \"refEntity\": {\n" + "          \"href\": \"/api/v2/refEntity\",\n"
			+ "          \"hrefCollection\": \"/api/v2/refEntity\",\n" + "          \"name\": \"refEntity\",\n"
			+ "          \"label\": \"refEntity\",\n" + "          \"attributes\": [\n" + "            {\n"
			+ "              \"href\": \"/api/v2/refEntity/meta/id\",\n" + "              \"fieldType\": \"STRING\",\n"
			+ "              \"name\": \"id\",\n" + "              \"label\": \"id\",\n"
			+ "              \"attributes\": [],\n" + "              \"maxLength\": 255,\n"
			+ "              \"auto\": false,\n" + "              \"nillable\": true,\n"
			+ "              \"readOnly\": true,\n" + "              \"labelAttribute\": false,\n"
			+ "              \"unique\": true,\n" + "              \"visible\": true,\n"
			+ "              \"lookupAttribute\": false,\n" + "              \"aggregateable\": false\n"
			+ "            },\n" + "            {\n" + "              \"href\": \"/api/v2/refEntity/meta/ref\",\n"
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
			+ "                    \"aggregateable\": false\n" + "                  }\n" + "                ],\n"
			+ "                \"labelAttribute\": \"id\",\n" + "                \"idAttribute\": \"id\",\n"
			+ "                \"lookupAttributes\": [],\n" + "                \"isAbstract\": false,\n"
			+ "                \"writable\": false\n" + "              },\n" + "              \"auto\": false,\n"
			+ "              \"nillable\": true,\n" + "              \"readOnly\": false,\n"
			+ "              \"labelAttribute\": false,\n" + "              \"unique\": false,\n"
			+ "              \"visible\": true,\n" + "              \"lookupAttribute\": false,\n"
			+ "              \"aggregateable\": false\n" + "            }\n" + "          ],\n"
			+ "          \"labelAttribute\": \"id\",\n" + "          \"idAttribute\": \"id\",\n"
			+ "          \"lookupAttributes\": [],\n" + "          \"isAbstract\": false,\n"
			+ "          \"writable\": false\n" + "        },\n" + "        \"auto\": false,\n"
			+ "        \"nillable\": true,\n" + "        \"readOnly\": false,\n"
			+ "        \"labelAttribute\": false,\n" + "        \"unique\": false,\n" + "        \"visible\": true,\n"
			+ "        \"lookupAttribute\": false,\n" + "        \"aggregateable\": false\n" + "      }\n" + "    ],\n"
			+ "    \"labelAttribute\": \"id\",\n" + "    \"idAttribute\": \"id\",\n" + "    \"lookupAttributes\": [],\n"
			+ "    \"isAbstract\": false,\n" + "    \"writable\": false\n" + "  },\n"
			+ "  \"_href\": \"/api/v2/entity/0\",\n" + "  \"xref\": {\n"
			+ "    \"_href\": \"/api/v2/refEntity/ref0\",\n" + "    \"id\": \"ref0\",\n" + "    \"ref\": {\n"
			+ "      \"_href\": \"/api/v2/refRefEntity/refRef0\",\n" + "      \"value\": \"value\"\n" + "    }\n"
			+ "  }\n" + "}";
}