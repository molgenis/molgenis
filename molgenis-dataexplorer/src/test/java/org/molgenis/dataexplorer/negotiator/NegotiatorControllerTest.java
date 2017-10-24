package org.molgenis.dataexplorer.negotiator;

import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.i18n.LanguageService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.rest.convert.QueryRsqlConverter;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfig;
import org.molgenis.dataexplorer.negotiator.config.NegotiatorEntityConfigMeta;
import org.molgenis.security.core.Permission;
import org.molgenis.security.core.PermissionService;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;

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
	private LanguageService languageService;
	@Mock
	private EntityType entityType;
	@Mock
	private NegotiatorEntityConfig negotiatorEntityConfig;

	@Captor
	private ArgumentCaptor<HttpEntity<NegotiatorQuery>> queryCaptor;

	@BeforeMethod
	public void beforeMethod()
	{
		controller = new NegotiatorController(restTemplate, permissionService, dataService, rsqlQueryConverter,
				languageService);
		Query<NegotiatorEntityConfig> query = new QueryImpl<NegotiatorEntityConfig>().eq(
				NegotiatorEntityConfigMeta.ENTITY, "blah");
		when(dataService.findOne(NegotiatorEntityConfigMeta.NEGOTIATORENTITYCONFIG, query,
				NegotiatorEntityConfig.class)).thenReturn(negotiatorEntityConfig);

	}

	@Test
	public void testExportToNegotiator() throws Exception
	{
		//				NegotiatorQuery query = NegotiatorQuery.create("http://molgenis.org/controller/callback",
		//						Lists.newArrayList(Collection.create("collId1", "biobankId1")), "Name contains Blah",
		//						"nToken");
		//
		//				when(directorySettings.getUsername()).thenReturn("username");
		//				when(directorySettings.getPassword()).thenReturn("password");
		//				when(directorySettings.getNegotiatorURL()).thenReturn("http://directory.com/postHere");
		//
		//				when(restTemplate.postForLocation(eq("http://directory.com/postHere"), queryCaptor.capture())).thenReturn(
		//						URI.create("http://directory.com/request/1280"));
		//
		//				assertEquals(controller.exportToNegotiator(query), "http://directory.com/request/1280");
		//
		//				HttpHeaders headers = new HttpHeaders();
		//				headers.setContentType(MediaType.APPLICATION_JSON);
		//				headers.set("Authorization", "Basic dXNlcm5hbWU6cGFzc3dvcmQ=");
		//				HttpEntity<NegotiatorQuery> posted = new HttpEntity<>(query, headers);
		//				assertEquals(queryCaptor.getValue(), posted);
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
		assertFalse(controller.showDirectoryButton("blah"));
	}

	@Test
	public void testShowButtonPermissionsOnPluginEntityNameMatches()
	{
		when(permissionService.hasPermissionOnPlugin("directory", Permission.READ)).thenReturn(false);
		when(entityType.getId()).thenReturn("blah");

		assertFalse(controller.showDirectoryButton("blah"));
	}

}
