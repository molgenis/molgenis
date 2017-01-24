package org.molgenis.dataexplorer.directory;

import com.google.common.collect.Lists;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.dataexplorer.controller.DirectoryController;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URI;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

public class DirectoryControllerTest
{
	private DirectoryController controller;
	@Mock
	private DirectorySettings directorySettings;
	@Mock
	private RestTemplate restTemplate;

	@Captor
	private ArgumentCaptor<HttpEntity<NegotiatorQuery>> queryCaptor;

	@BeforeClass
	public void beforeClass()
	{
		MockitoAnnotations.initMocks(this);
		controller = new DirectoryController(directorySettings, restTemplate);
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

		when(restTemplate.postForLocation(eq("http://directory.com/postHere"), queryCaptor.capture()))
				.thenReturn(URI.create("http://directory.com/request/1280"));

		assertEquals(controller.exportToNegotiator(query), "http://directory.com/request/1280");

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Basic dXNlcm5hbWU6cGFzc3dvcmQ=");
		HttpEntity<NegotiatorQuery> posted = new HttpEntity<>(query, headers);
		assertEquals(queryCaptor.getValue(), posted);
	}

}
