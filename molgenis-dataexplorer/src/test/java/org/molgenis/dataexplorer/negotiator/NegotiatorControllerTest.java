package org.molgenis.dataexplorer.negotiator;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.quality.Strictness;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.Query;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.convert.QueryRsqlConverter;
import org.molgenis.data.rsql.QueryRsql;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorConfig;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfig;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfigMeta;
import org.molgenis.js.magma.JsMagmaScriptEvaluator;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.molgenis.test.AbstractMockitoTest;
import org.molgenis.util.GsonConfig;
import org.springframework.context.MessageSource;
import org.springframework.context.support.MessageSourceResourceBundle;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

@WebAppConfiguration
@ContextConfiguration(classes = { GsonConfig.class })
public class NegotiatorControllerTest extends AbstractMockitoTest
{
	private NegotiatorController controller;
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private PermissionService permissionService;
	@Mock
	private DataService dataService;
	@Mock
	private QueryRsqlConverter rsqlQueryConverter;
	@Mock
	private EntityType entityType;
	@Mock
	private NegotiatorEntityConfig negotiatorEntityConfig;
	@Mock
	private NegotiatorConfig negotiatorConfig;
	@Mock
	private QueryRsql queryRsql;
	@Mock
	private Attribute collectionAttr;
	@Mock
	private Attribute biobackAttr;
	@Mock
	private Attribute enabledAttr;
	@Mock
	private Query<Entity> molgenisQuery;
	@Mock
	private LanguageService languageService;
	@Mock
	private MessageSource messageSource;
	@Mock
	private JsMagmaScriptEvaluator jsMagmaScriptEvaluator;

	@Captor
	private ArgumentCaptor<HttpEntity<NegotiatorQuery>> queryCaptor;

	public NegotiatorControllerTest()
	{
		super(Strictness.WARN);
	}

	@BeforeMethod
	public void beforeMethod()
	{

		controller = new NegotiatorController(restTemplate, permissionService, dataService, rsqlQueryConverter,
				languageService, jsMagmaScriptEvaluator);
		Query<NegotiatorEntityConfig> query = new QueryImpl<NegotiatorEntityConfig>().eq(NegotiatorEntityConfigMeta.ENTITY, "blah");

		when(dataService.getEntityType("blah")).thenReturn(entityType);
		when(dataService.findOne(NegotiatorEntityConfigMeta.NEGOTIATORENTITYCONFIG, query,
				NegotiatorEntityConfig.class)).thenReturn(negotiatorEntityConfig);
		when(negotiatorEntityConfig.getNegotiatorConfig()).thenReturn(negotiatorConfig);
		when(rsqlQueryConverter.convert("*=q=MOLGENIS")).thenReturn(queryRsql);
		when(queryRsql.createQuery(entityType)).thenReturn(molgenisQuery);
		when(negotiatorEntityConfig.getEntity(NegotiatorEntityConfigMeta.COLLECTION_ID, Attribute.class)).thenReturn(
				collectionAttr);
		when(collectionAttr.getName()).thenReturn("collectionAttr");
		when(collectionAttr.getDataType()).thenReturn(AttributeType.STRING);
		when(negotiatorEntityConfig.getEntity(NegotiatorEntityConfigMeta.BIOBANK_ID, Attribute.class)).thenReturn(
				biobackAttr);
		when(biobackAttr.getName()).thenReturn("biobackAttr");
		when(biobackAttr.getDataType()).thenReturn(AttributeType.STRING);
		when(negotiatorEntityConfig.getString(NegotiatorEntityConfigMeta.ENABLED_EXPRESSION)).thenReturn(
				"$(enabledAttr).value()");
		when(enabledAttr.getName()).thenReturn("enabledAttr");
		when(languageService.getBundle()).thenReturn(new MessageSourceResourceBundle(messageSource, Locale.ENGLISH));
		when(messageSource.getMessage("dataexplorer_directory_no_rows", null, Locale.ENGLISH)).thenReturn("No Rows");
		when(messageSource.getMessage("dataexplorer_directory_disabled", null, Locale.ENGLISH)).thenReturn(
				"Disabled %s");
		when(messageSource.getMessage("dataexplorer_directory_no_config", null, Locale.ENGLISH)).thenReturn(
				"No Config");

		when(entityType.getLabelAttribute()).thenReturn(collectionAttr);
	}

	@Test
	public void testExportToNegotiatorNoCollections() throws Exception
	{
		NegotiatorRequest negotiatorRequest = NegotiatorRequest.create("http://tester.test.tst", "blah", "*=q=MOLGENIS",
				"human", "token");
		NegotiatorQuery query = NegotiatorQuery.create("http://molgenis.org/controller/callback",
				Lists.newArrayList(Collection.create("collId1", "biobankId1")), "Name contains Blah", "nToken");

		when(negotiatorConfig.getUsername()).thenReturn("username");
		when(negotiatorConfig.getPassword()).thenReturn("password");
		when(negotiatorConfig.getNegotiatorURL()).thenReturn("http://directory.com/postHere");

		when(restTemplate.postForLocation(eq("http://directory.com/postHere"), queryCaptor.capture())).thenReturn(
				URI.create("http://directory.com/request/1280"));
		ExportResponse expected = ExportResponse.create(false, "No Rows", "");

		assertEquals(controller.exportToNegotiator(negotiatorRequest), expected);
	}

	@Test
	public void testExportToNegotiatorDisabledCollections() throws Exception
	{
		Entity entity1 = mock(Entity.class);
		when(entity1.get("enabledAttr")).thenReturn("true");
		when(entity1.getLabelValue()).thenReturn("label1");
		when(entity1.get("collectionAttr")).thenReturn("coll1");
		when(entity1.get("biobackAttr")).thenReturn("bio1");
		when(entity1.get("enabledAttr")).thenReturn("true");
		Entity entity2 = mock(Entity.class);
		when(entity2.get("enabledAttr")).thenReturn(new Boolean(true));
		when(entity2.getLabelValue()).thenReturn("label2");
		when(entity2.get("collectionAttr")).thenReturn("coll2");
		when(entity2.get("biobackAttr")).thenReturn("bio2");
		when(entity2.get("enabledAttr")).thenReturn("false");

		when(dataService.getEntityType("blah")).thenReturn(entityType);
		List collections = new ArrayList();
		collections.add(entity1);
		collections.add(entity2);
		when(entityType.getId()).thenReturn("blah");
		when(dataService.findAll("blah", molgenisQuery)).thenReturn(collections.stream());

		NegotiatorRequest negotiatorRequest = NegotiatorRequest.create("http://tester.test.tst", "blah", "*=q=MOLGENIS",
				"human", "token");

		when(negotiatorConfig.getUsername()).thenReturn("username");
		when(negotiatorConfig.getPassword()).thenReturn("password");
		when(negotiatorConfig.getNegotiatorURL()).thenReturn("http://directory.com/postHere");

		when(jsMagmaScriptEvaluator.eval("$(enabledAttr).value()", entity1)).thenReturn(Boolean.TRUE);
		when(jsMagmaScriptEvaluator.eval("$(enabledAttr).value()", entity2)).thenReturn(Boolean.FALSE);

		ExportResponse expected = ExportResponse.create(false, "Disabled label2", "");

		assertEquals(controller.exportToNegotiator(negotiatorRequest), expected);
	}

	@Test
	public void testExportToNegotiator() throws Exception
	{
		Entity entity1 = mock(Entity.class);
		when(entity1.get("enabledAttr")).thenReturn("true");
		when(entity1.getLabelValue()).thenReturn("label1");
		when(entity1.get("collectionAttr")).thenReturn("coll1");
		when(entity1.get("biobackAttr")).thenReturn("bio1");
		when(entity1.get("enabledAttr")).thenReturn(true);

		Entity entity2 = mock(Entity.class);
		when(entity2.get("enabledAttr")).thenReturn(new Boolean(true));
		when(entity2.getLabelValue()).thenReturn("label2");
		when(entity2.get("collectionAttr")).thenReturn("coll2");
		when(entity2.get("biobackAttr")).thenReturn("bio2");
		when(entity2.get("enabledAttr")).thenReturn("true");
		when(dataService.getEntityType("blah")).thenReturn(entityType);
		List collections = new ArrayList();
		collections.add(entity1);
		collections.add(entity2);
		when(entityType.getId()).thenReturn("blah");
		when(dataService.findAll("blah", molgenisQuery)).thenReturn(collections.stream());

		NegotiatorRequest negotiatorRequest = NegotiatorRequest.create("http://tester.test.tst", "blah", "*=q=MOLGENIS",
				"human", "token");

		when(negotiatorConfig.getUsername()).thenReturn("username");
		when(negotiatorConfig.getPassword()).thenReturn("password");
		when(negotiatorConfig.getNegotiatorURL()).thenReturn("http://directory.com/postHere");

		when(restTemplate.postForLocation(eq("http://directory.com/postHere"), queryCaptor.capture())).thenReturn(
				URI.create("http://directory.com/request/1280"));
		when(jsMagmaScriptEvaluator.eval(any(), any())).thenReturn(Boolean.TRUE);
		ExportResponse expected = ExportResponse.create(true, "", "http://directory.com/request/1280");

		assertEquals(controller.exportToNegotiator(negotiatorRequest), expected);

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Basic dXNlcm5hbWU6cGFzc3dvcmQ=");
		HttpEntity<NegotiatorQuery> posted = new HttpEntity<>(NegotiatorQuery.create("http://tester.test.tst",
				Arrays.asList(Collection.create("coll1", "bio1"), Collection.create("coll2", "bio2")), "human",
				"token"), headers);
		assertEquals(queryCaptor.getValue(), posted);
	}

	@Test
	public void testShowButtonNoPermissionsOnPlugin()
	{
		when(permissionService.hasPermissionOnPlugin("directory", Permission.READ)).thenReturn(false);
		assertFalse(controller.showDirectoryButton("blah"));
	}

	@Test
	public void testShowButton()
	{
		when(permissionService.hasPermissionOnPlugin("directory", Permission.READ)).thenReturn(true);
		assertTrue(controller.showDirectoryButton("blah"));
	}

	@Test
	public void testShowButtonPermissionsOnPluginNoConfig()
	{
		when(permissionService.hasPermissionOnPlugin("directory", Permission.READ)).thenReturn(false);
		when(entityType.getId()).thenReturn("blah2");

		assertFalse(controller.showDirectoryButton("blah2"));
	}

}
