package org.molgenis.pathways.service;

import com.google.common.collect.ImmutableMap;
import org.molgenis.pathways.model.Impact;
import org.molgenis.pathways.model.Pathway;
import org.molgenis.wikipathways.client.WSPathway;
import org.molgenis.wikipathways.client.WSPathwayInfo;
import org.molgenis.wikipathways.client.WSSearchResult;
import org.molgenis.wikipathways.client.WikiPathwaysPortType;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

public class WikiPathwaysServiceTest
{
	private WikiPathwaysService wikiPathwaysService;
	private WikiPathwaysPortType wikiPathwaysPortType;

	@BeforeTest
	public void beforeTest()
	{
		wikiPathwaysPortType = mock(WikiPathwaysPortType.class);
		wikiPathwaysService = new WikiPathwaysService(wikiPathwaysPortType);
	}

	@Test
	public void testGetAllPathways() throws ExecutionException, RemoteException
	{
		WSPathwayInfo pathway1 = new WSPathwayInfo("WP1234", "http://pathways.org/WP1234", "Blahdi Pathway",
				"Homo sapiens", "0");
		WSPathwayInfo pathway2 = new WSPathwayInfo("WP1235", "http://pathways.org/WP1235", "Yet another Pathway",
				"Homo sapiens", "0");
		when(wikiPathwaysPortType.listPathways("Homo sapiens")).thenReturn(new WSPathwayInfo[] { pathway1, pathway2 });
		List<Pathway> expected = Arrays.asList(Pathway.create("WP1234", "Blahdi Pathway"),
				Pathway.create("WP1235", "Yet another Pathway"));
		assertEquals(wikiPathwaysService.getAllPathways("Homo sapiens"), expected);
		// check that results are cached
		assertEquals(wikiPathwaysService.getAllPathways("Homo sapiens"), expected);
		verify(wikiPathwaysPortType).listPathways("Homo sapiens");
	}

	@Test
	public void testGetFilteredPathways() throws RemoteException
	{
		WSSearchResult pathway1 = new WSSearchResult(100, null, "WP1234", "http://pathways.org/WP1234",
				"Blahdi Pathway", "Homo sapiens", "2352");
		WSSearchResult pathway2 = new WSSearchResult(200, null, "WP1235", "http://pathways.org/WP1235",
				"Yet another Pathway", "Homo sapiens", "1124");
		when(wikiPathwaysPortType.findPathwaysByText("cancer", "Homo sapiens")).thenReturn(
				new WSSearchResult[] { pathway1, pathway2 });
		List<Pathway> expected = Arrays.asList(Pathway.create("WP1234", "Blahdi Pathway"),
				Pathway.create("WP1235", "Yet another Pathway"));
		assertEquals(wikiPathwaysService.getFilteredPathways("cancer", "Homo sapiens"), expected);
	}

	@Test
	public void testGetPathwaysForGene() throws ExecutionException, RemoteException
	{
		when(wikiPathwaysPortType.findPathwaysByXref(new String[] { "ABCD1" }, new String[] { "H" })).thenReturn(
				new WSSearchResult[] {
						new WSSearchResult(100, null, "WP1234", null, "Blahdi Pathway", "Homo sapiens", "8921"),
						new WSSearchResult(100, null, "WP6543", null, "Ratti Pathway", "Rattus norvegicus", "231"),
						new WSSearchResult(100, null, "WP1235", null, "Yet another Pathway", "Homo sapiens", "8922") });
		List<Pathway> expected = Arrays.asList(Pathway.create("WP1234", "Blahdi Pathway"),
				Pathway.create("WP1235", "Yet another Pathway"));
		assertEquals(wikiPathwaysService.getPathwaysForGene("ABCD1", "Homo sapiens"), expected);
		// test that results are cached
		assertEquals(wikiPathwaysService.getPathwaysForGene("ABCD1", "Homo sapiens"), expected);
		verify(wikiPathwaysPortType).findPathwaysByXref(new String[] { "ABCD1" }, new String[] { "H" });
	}

	@Test
	public void testGetPathwayGPML() throws RemoteException, UnsupportedEncodingException
	{
		when(wikiPathwaysPortType.getPathway("WP1234", 0)).thenReturn(
				new WSPathway("<gpml>bl\u00ebh</gpml>", "WP1234", "http://pathways.org/WP1234", "Blahdi Pathway",
						"Homo sapiens", "2352"));
		assertEquals(wikiPathwaysService.getPathwayGPML("WP1234"), "<gpml>bl\u00ebh</gpml>");
	}

	@Test
	public void testGetColoredPathwayImage() throws ExecutionException, RemoteException, UnsupportedEncodingException
	{
		String svg = "<svg>bl\u00ebah</svg> ";
		when(wikiPathwaysPortType.getColoredPathway("WP1234", "0", new String[] { "graphID1", "graphID2" },
				new String[] { Impact.HIGH.getColor(), Impact.MODERATE.getColor() }, "svg")).thenReturn(
				"<svg>bl\u00ebah</svg>\n".getBytes("UTF-8"));
		assertEquals(wikiPathwaysService.getColoredPathwayImage("WP1234",
				ImmutableMap.<String, Impact>of("graphID1", Impact.HIGH, "graphID2", Impact.MODERATE)), svg);
		// test that result gets cached
		assertEquals(wikiPathwaysService.getColoredPathwayImage("WP1234",
				ImmutableMap.<String, Impact>of("graphID1", Impact.HIGH, "graphID2", Impact.MODERATE)), svg);
		verify(wikiPathwaysPortType).getColoredPathway("WP1234", "0", new String[] { "graphID1", "graphID2" },
				new String[] { Impact.HIGH.getColor(), Impact.MODERATE.getColor() }, "svg");
	}

	@Test
	public void testGetUncoloredPathway() throws ExecutionException, RemoteException, UnsupportedEncodingException
	{
		when(wikiPathwaysPortType.getPathwayAs("svg", "WP1234", 0)).thenReturn(
				"<svg>WP1234\u00eb</svg>".getBytes("UTF-8"));
		assertEquals(wikiPathwaysService.getUncoloredPathwayImage("WP1234"), "<svg>WP1234\u00eb</svg> ");
		// test that result gets cached
		assertEquals(wikiPathwaysService.getUncoloredPathwayImage("WP1234"), "<svg>WP1234\u00eb</svg> ");
		verify(wikiPathwaysPortType).getPathwayAs("svg", "WP1234", 0);
	}
}
