package org.molgenis.framework.server.services;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.IOException;
import java.text.ParseException;
import java.util.Vector;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.server.MolgenisContext;
import org.molgenis.framework.server.MolgenisRequest;
import org.molgenis.framework.server.MolgenisResponse;
import org.molgenis.mock.MockDatabase;
import org.molgenis.mock.MockLogin;
import org.molgenis.model.elements.Entity;
import org.molgenis.model.elements.Model;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MolgenisDownloadServiceTest
{
	private static final String API_FIND_URI = "/api/find";

	private MolgenisDownloadService molgenisDownloadService;
	private MockHttpServletRequest mockHttpServletRequest;
	private MockHttpServletResponse mockHttpServletResponse;
	private MolgenisRequest molgenisRequest;
	private MolgenisResponse molgenisResponse;

	@BeforeMethod
	public void setup() throws Exception
	{
		molgenisDownloadService = new MolgenisDownloadService(mock(MolgenisContext.class));
		mockHttpServletRequest = new MockHttpServletRequest();
		mockHttpServletResponse = new MockHttpServletResponse();
		molgenisRequest = new MolgenisRequest(mockHttpServletRequest);
		molgenisResponse = new MolgenisResponse(mockHttpServletResponse);
	}

	@Test
	public void testNotAuthenticated() throws Exception
	{
		MockDatabase db = new MockDatabase();
		db.setLogin(new MockLogin(false));

		Model model = when(mock(Model.class).getEntities(false, false)).thenReturn(new Vector<Entity>()).getMock();
		db.setMetaData(model);

		molgenisRequest.setDatabase(db);
		molgenisRequest.setServicePath(API_FIND_URI);
		mockHttpServletRequest.setPathInfo(API_FIND_URI + "/");

		molgenisDownloadService.handleRequest(molgenisRequest, molgenisResponse);

		assertEquals(mockHttpServletResponse.getStatus(), 200);

		String response = mockHttpServletResponse.getContentAsString();
		assertTrue((response != null) && response.contains("You are currently browsing as anonymous."));
	}

	@Test
	public void testAuthenticated() throws Exception
	{
		MockDatabase db = new MockDatabase();
		db.setLogin(new MockLogin(true));

		Model model = when(mock(Model.class).getEntities(false, false)).thenReturn(new Vector<Entity>()).getMock();
		db.setMetaData(model);

		molgenisRequest.setDatabase(db);
		molgenisRequest.setServicePath(API_FIND_URI);
		mockHttpServletRequest.setPathInfo(API_FIND_URI + "/");

		molgenisDownloadService.handleRequest(molgenisRequest, molgenisResponse);

		assertEquals(mockHttpServletResponse.getStatus(), 200);

		String response = mockHttpServletResponse.getContentAsString();
		assertTrue((response != null) && !response.contains("You are currently browsing as anonymous."));
	}

	@Test
	public void testNotExistingOrSystemEntity() throws ParseException, DatabaseException, IOException
	{
		MockDatabase db = new MockDatabase();
		db.setLogin(new MockLogin(true));

		Model model = when(mock(Model.class).getEntities(false, false)).thenReturn(new Vector<Entity>()).getMock();
		db.setMetaData(model);

		molgenisRequest.setDatabase(db);
		molgenisRequest.setServicePath(API_FIND_URI);
		mockHttpServletRequest.setPathInfo(API_FIND_URI + "/XXXX");

		molgenisDownloadService.handleRequest(molgenisRequest, molgenisResponse);

		assertEquals(mockHttpServletResponse.getStatus(), 404);
	}

}
