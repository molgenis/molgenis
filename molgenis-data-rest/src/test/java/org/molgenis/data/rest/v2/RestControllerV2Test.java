package org.molgenis.data.rest.v2;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.molgenis.MolgenisFieldTypes.BOOL;
import static org.molgenis.MolgenisFieldTypes.CATEGORICAL;
import static org.molgenis.MolgenisFieldTypes.CATEGORICAL_MREF;
import static org.molgenis.MolgenisFieldTypes.COMPOUND;
import static org.molgenis.MolgenisFieldTypes.DATE;
import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.DECIMAL;
import static org.molgenis.MolgenisFieldTypes.EMAIL;
import static org.molgenis.MolgenisFieldTypes.ENUM;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Collections;

import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.rest.v2.RestControllerV2Test.RestControllerV2Config;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.util.GsonHttpMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.support.FormattingConversionService;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@WebAppConfiguration
@ContextConfiguration(classes = RestControllerV2Config.class)
public class RestControllerV2Test extends AbstractTestNGSpringContextTests
{
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
	private DataService dataService;

	private MockMvc mockMvc;
	private String attrBool;
	private String attrString;
	private String attrXref;
	private String refAttrValue;
	private String refAttrId;
	private String refAttrRef;
	private String refRefAttrValue;

	@BeforeMethod
	public void beforeMethod()
	{
		String refRefAttrId = "id";
		refRefAttrValue = "value";
		DefaultEntityMetaData refRefEntityMetaData = new DefaultEntityMetaData(REF_REF_ENTITY_NAME);
		refRefEntityMetaData.addAttribute(refRefAttrId).setDataType(STRING).setIdAttribute(true);
		refRefEntityMetaData.addAttribute(refRefAttrValue).setDataType(STRING);

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
		String attrCompound = "compound";
		String attrCompoundAttr0 = "compound_attr0";
		String attrCompoundAttrCompound = "compound_attrcompound";
		String attrCompoundAttrCompoundAttr0 = "compound_attrcompound_attr0";
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
		entityMetaData.addAttribute(attrDecimal).setDataType(DECIMAL);
		entityMetaData.addAttribute(attrEmail).setDataType(EMAIL);
		entityMetaData.addAttribute(attrEnum).setDataType(ENUM).setEnumOptions(Arrays.asList(enum0, enum1, enum2));
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
		entityMetaData.addAttribute(attrEnumOptional).setDataType(ENUM)
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
		when(dataService.count(ENTITY_NAME, q)).thenReturn(2l);
		when(dataService.findAll(ENTITY_NAME, q)).thenReturn(Arrays.asList(entity));
		when(dataService.findOne(REF_ENTITY_NAME, REF_ENTITY0_ID)).thenReturn(refEntity0);
		when(dataService.findOne(REF_ENTITY_NAME, REF_ENTITY1_ID)).thenReturn(refEntity1);
		when(dataService.findOne(REF_REF_ENTITY_NAME, REF_REF_ENTITY_ID)).thenReturn(refRefEntity);
		when(dataService.getEntityMetaData(ENTITY_NAME)).thenReturn(entityMetaData);
		when(dataService.getEntityMetaData(REF_ENTITY_NAME)).thenReturn(refEntityMetaData);
		when(dataService.getEntityMetaData(REF_REF_ENTITY_NAME)).thenReturn(refRefEntityMetaData);

		boolean prettyPrint = true;
		mockMvc = MockMvcBuilders.standaloneSetup(restControllerV2)
				.setMessageConverters(new GsonHttpMessageConverter(prettyPrint))
				.setConversionService(conversionService).build();
	}

	@Test
	public void retrieveResource() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON)).andExpect(content().string("FIXME"));
	}

	@Test
	public void retrieveResourcePartialResponseAttribute() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attributes", attrBool)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON)).andExpect(content().string("FIXME"));
	}

	@Test
	public void retrieveResourcePartialResponseAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attributes", attrBool + ',' + attrString))
				.andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string("FIXME"));
	}

	@Test
	public void retrieveResourcePartialResponseSubAttribute() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attributes", attrXref + '(' + refAttrValue + ')'))
				.andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string("FIXME"));
	}

	@Test
	public void retrieveResourcePartialResponseSubAttributes() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_ID).param("attributes", attrXref + '(' + refAttrId + ',' + refAttrValue + ')'))
				.andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string("FIXME"));
	}

	@Test
	public void retrieveResourcePartialResponseSubSubAttributes() throws Exception
	{
		mockMvc.perform(
				get(HREF_ENTITY_ID).param("attributes",
						attrXref + '(' + refAttrId + ',' + refAttrRef + '(' + refRefAttrValue + ')' + ')'))
				.andExpect(status().isOk()).andExpect(content().contentType(APPLICATION_JSON))
				.andExpect(content().string("FIXME"));
	}

	@Test
	public void retrieveResourceCollection() throws Exception
	{
		mockMvc.perform(get(HREF_ENTITY_COLLECTION)).andExpect(status().isOk())
				.andExpect(content().contentType(APPLICATION_JSON)).andExpect(content().string("FIXME"));
	}

	@Test
	public void retrieveResourceCollectionPartialResponseAttribute() throws Exception
	{
		// items
	}

	@Test
	public void retrieveResourceCollectionPartialResponseAttributes() throws Exception
	{
		// etag,items
	}

	@Test
	public void retrieveResourceCollectionPartialResponseSubAttribute() throws Exception
	{
		// items(title)
	}

	@Test
	public void retrieveResourceCollectionPartialResponseSubAttributes() throws Exception
	{
		// items(title,author/uri)
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
		public RestControllerV2 restController()
		{
			return new RestControllerV2(dataService(), molgenisPermissionService());
		}
	}
}
