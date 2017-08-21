package org.molgenis.searchall.controller;

import org.molgenis.data.DataService;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;

public class SearchAllControllerTest
{
	DataService dataService;
	SearchAllController searchAllController;

	@BeforeClass
	public void setUp()
	{
		dataService = mock(DataService.class);
		searchAllController = new SearchAllController(dataService);
	}

	@Test
	public void testFindAll() throws Exception
	{
		//TODO: implement...
	}

}