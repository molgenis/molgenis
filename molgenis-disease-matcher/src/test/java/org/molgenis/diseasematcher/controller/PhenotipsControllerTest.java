package org.molgenis.diseasematcher.controller;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;

import javax.servlet.ServletOutputStream;

import org.mockito.Mockito;
import org.molgenis.diseasematcher.service.PhenotipsService;
import org.springframework.mock.web.MockHttpServletResponse;
import org.testng.annotations.Test;

public class PhenotipsControllerTest
{

	@Test
	public void getPhenotipsRankingTest()
	{
		PhenotipsService phenotipsService = mock(PhenotipsService.class);
		PhenotipsController pcc = new PhenotipsController(phenotipsService);

		MockHttpServletResponse response = new MockHttpServletResponse();
		ArrayList<String> terms = new ArrayList<String>();
		terms.add("HP:0000252");
		terms.add("HP:0004322");
		terms.add("HP:0009900");
		pcc.getPhenotipsRanking(terms, response);

		assertEquals(response.getContentType(), "text/html");

		verify(phenotipsService).getRanking(eq(terms), Mockito.any(ServletOutputStream.class));
	}
}
