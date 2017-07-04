package org.molgenis.dataexplorer.directory;

import com.google.common.collect.Lists;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.dataexplorer.controller.DirectoryController;
import org.molgenis.security.core.MolgenisPermissionService;
import org.molgenis.security.core.Permission;
import org.molgenis.test.AbstractMockitoTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.net.URI;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class DirectoryControllerTest extends AbstractMockitoTest
{
	private DirectoryController controller;
	@Mock
	private DirectorySettings directorySettings;
	@Mock
	private RestTemplate restTemplate;
	@Mock
	private MolgenisPermissionService permissions;
	@Mock
	private EntityType entityType;

	@Captor
	private ArgumentCaptor<HttpEntity<NegotiatorQuery>> queryCaptor;

	@BeforeMethod
	public void beforeMethod()
	{
		controller = new DirectoryController(directorySettings, restTemplate, permissions);
	}

	@Test
	public void testExportToNegotiator() throws Exception
	{
		NegotiatorQuery query = NegotiatorQuery.createQuery("http://molgenis.org/controller/callback",
				Lists.newArrayList(Collection.createCollection("collId1", "biobankId1")), "Name contains Blah",
				"nToken");

		when(directorySettings.getUsername()).thenReturn("username");
		when(directorySettings.getPassword()).thenReturn("password");
		when(directorySettings.getNegotiatorURL()).thenReturn("http://directory.com/postHere");

		when(restTemplate.postForLocation(eq("http://directory.com/postHere"), queryCaptor.capture())).thenReturn(
				URI.create("http://directory.com/request/1280"));

		assertEquals(controller.exportToNegotiator(query), "http://directory.com/request/1280");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Basic dXNlcm5hbWU6cGFzc3dvcmQ=");
		HttpEntity<NegotiatorQuery> posted = new HttpEntity<>(query, headers);
		assertEquals(queryCaptor.getValue(), posted);
	}

	@Test
	public void testShowButtonNoPermissionsOnPlugin()
	{
		when(permissions.hasPermissionOnPlugin("directory", Permission.READ)).thenReturn(false);

		assertFalse(controller.showDirectoryButton("blah"));
	}

	@Test
	public void testShowButtonPermissionsOnPluginButNoEntity()
	{
		when(permissions.hasPermissionOnPlugin("directory", Permission.READ)).thenReturn(true);
		when(directorySettings.getCollectionEntityType()).thenReturn(null);

		assertFalse(controller.showDirectoryButton("blah"));
	}

	@Test
	public void testShowButtonPermissionsOnPluginButWrongEntity()
	{
		when(permissions.hasPermissionOnPlugin("directory", Permission.READ)).thenReturn(false);
		when(directorySettings.getCollectionEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn("Other");

		assertFalse(controller.showDirectoryButton("blah"));
	}

	@Test
	public void testShowButtonPermissionsOnPluginEntityNameMatches()
	{
		when(permissions.hasPermissionOnPlugin("directory", Permission.READ)).thenReturn(false);
		when(directorySettings.getCollectionEntityType()).thenReturn(entityType);
		when(entityType.getId()).thenReturn("blah");

		assertFalse(controller.showDirectoryButton("blah"));
	}

}
