package org.molgenis.data.algorithm;

import static org.testng.Assert.assertEquals;

import java.util.Collections;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class AlgorithmServiceImplTest
{
	private AlgorithmService algorithmService;

	@BeforeTest
	public void beforeTest()
	{
		algorithmService = new AlgorithmServiceImpl();
	}

	@Test
	public void testGetSourceAttributeNames()
	{
		assertEquals(algorithmService.getSourceAttributeNames("$('id')"), Collections.singletonList("id"));
	}

	@Test
	public void testGetSourceAttributeNamesNoQuotes()
	{
		assertEquals(algorithmService.getSourceAttributeNames("$(id)"), Collections.singletonList("id"));
	}
}
