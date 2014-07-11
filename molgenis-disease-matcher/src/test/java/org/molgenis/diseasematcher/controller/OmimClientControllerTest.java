package org.molgenis.diseasematcher.controller;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.AssertJUnit.assertEquals;

import java.io.OutputStream;

import org.mockito.Mockito;
import org.molgenis.diseasematcher.service.OmimService;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.Test;

public class OmimClientControllerTest
{

	@Test
	public void testGetOmimData()
	{
		OmimService omimService = mock(OmimService.class);
		OmimClientController occ = new OmimClientController(omimService);

		MockHttpServletResponse response = new MockHttpServletResponse();
		occ.getOmimData("12345", response);

		assertEquals(response.getContentType(), "application/json");

		verify(omimService).getOmimData(eq("12345"), Mockito.any(OutputStream.class));
	}
}
