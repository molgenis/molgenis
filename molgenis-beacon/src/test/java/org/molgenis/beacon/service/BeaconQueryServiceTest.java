package org.molgenis.beacon.service;

import com.google.common.collect.Lists;
import org.mockito.Mock;
import org.molgenis.beacon.config.Beacon;
import org.molgenis.beacon.config.BeaconDataset;
import org.molgenis.beacon.config.BeaconMetadata;
import org.molgenis.beacon.controller.model.BeaconAlleleRequest;
import org.molgenis.beacon.controller.model.BeaconAlleleResponse;
import org.molgenis.beacon.controller.model.exceptions.BeaconException;
import org.molgenis.beacon.controller.model.exceptions.NestedBeaconException;
import org.molgenis.beacon.service.impl.BeaconQueryServiceImpl;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

public class BeaconQueryServiceTest
{
	private BeaconQueryService beaconQueryService;

	@Mock
	private DataService dataService;

	private Query query1;
	private Query query2;

	private BeaconDataset dataset1;
	private BeaconDataset dataset2;

	private static final String BEACON_ID = "beacon";

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);

		dataset1 = mock(BeaconDataset.class, RETURNS_DEEP_STUBS);
		when(dataset1.getId()).thenReturn("dataset1");
		when(dataset1.getDatasetEntityType().getId()).thenReturn("dataset1");
		when(dataset1.getGenomeBrowserAttributes().getChrom()).thenReturn("#CHROM");
		when(dataset1.getGenomeBrowserAttributes().getPos()).thenReturn("POS");
		when(dataset1.getGenomeBrowserAttributes().getRef()).thenReturn("REF");
		when(dataset1.getGenomeBrowserAttributes().getAlt()).thenReturn("ALT");

		dataset2 = mock(BeaconDataset.class, RETURNS_DEEP_STUBS);
		when(dataset2.getId()).thenReturn("dataset2");
		when(dataset2.getDatasetEntityType().getId()).thenReturn("dataset2");
		when(dataset2.getGenomeBrowserAttributes().getChrom()).thenReturn("#CHROM");
		when(dataset2.getGenomeBrowserAttributes().getPos()).thenReturn("POS");
		when(dataset2.getGenomeBrowserAttributes().getRef()).thenReturn("REF");
		when(dataset2.getGenomeBrowserAttributes().getAlt()).thenReturn("ALT");

		query1 = new QueryImpl<>().eq(dataset1.getGenomeBrowserAttributes().getChrom(), "1")
								  .and()
								  .eq(dataset1.getGenomeBrowserAttributes().getPos(), 100L)
								  .and()
								  .eq(dataset1.getGenomeBrowserAttributes().getRef(), "A")
								  .and()
								  .eq(dataset1.getGenomeBrowserAttributes().getAlt(), "T");

		query2 = new QueryImpl<>().eq(dataset2.getGenomeBrowserAttributes().getChrom(), "1")
								  .and()
								  .eq(dataset2.getGenomeBrowserAttributes().getPos(), 100L)
								  .and()
								  .eq(dataset2.getGenomeBrowserAttributes().getRef(), "A")
								  .and()
								  .eq(dataset2.getGenomeBrowserAttributes().getAlt(), "T");

		beaconQueryService = new BeaconQueryServiceImpl(dataService);
	}

	@Test
	public void getQueryExistsTest()
	{
		Beacon beacon = mock(Beacon.class);
		when(beacon.getDataSets()).thenReturn(Lists.newArrayList(dataset1, dataset2));

		when(dataService.findOneById(BeaconMetadata.BEACON, BEACON_ID, Beacon.class)).thenReturn(beacon);

		doReturn(0L).when(dataService).count("dataset1", query1);
		doReturn(1L).when(dataService).count("dataset2", query2);

		BeaconAlleleRequest request = BeaconAlleleRequest.create("1", 100L, "A", "T");

		BeaconAlleleResponse actualResponse = beaconQueryService.query("1", 100L, "A", "T", BEACON_ID);
		BeaconAlleleResponse expectedResponse = BeaconAlleleResponse.create(BEACON_ID, true, null, request);

		assertEquals(actualResponse, expectedResponse);
	}

	@Test
	public void postQueryExistsTest()
	{
		Beacon beacon = mock(Beacon.class);
		when(beacon.getDataSets()).thenReturn(Lists.newArrayList(dataset1, dataset2));

		when(dataService.findOneById(BeaconMetadata.BEACON, BEACON_ID, Beacon.class)).thenReturn(beacon);

		doReturn(0L).when(dataService).count("dataset1", query1);
		doReturn(1L).when(dataService).count("dataset2", query2);

		BeaconAlleleRequest request = BeaconAlleleRequest.create("1", 100L, "A", "T");

		BeaconAlleleResponse actualResponse = beaconQueryService.query(BEACON_ID, request);
		BeaconAlleleResponse expectedResponse = BeaconAlleleResponse.create(BEACON_ID, true, null, request);

		assertEquals(actualResponse, expectedResponse);
	}

	@Test
	public void queryNotExistsTest()
	{
		Beacon beacon = mock(Beacon.class);
		when(beacon.getDataSets()).thenReturn(Lists.newArrayList(dataset1, dataset2));

		when(dataService.findOneById(BeaconMetadata.BEACON, BEACON_ID, Beacon.class)).thenReturn(beacon);

		doReturn(0L).when(dataService).count("dataset1", query1);
		doReturn(0L).when(dataService).count("dataset2", query2);

		BeaconAlleleRequest request = BeaconAlleleRequest.create("1", 100L, "A", "T");

		BeaconAlleleResponse actualResponse = beaconQueryService.query(BEACON_ID, request);
		BeaconAlleleResponse expectedResponse = BeaconAlleleResponse.create(BEACON_ID, false, null, request);

		assertEquals(actualResponse, expectedResponse);
	}

	@Test
	public void queryErrorTest()
	{
		MolgenisDataException exception = new MolgenisDataException("Error test");
		when(dataService.findOneById(BeaconMetadata.BEACON, BEACON_ID, Beacon.class)).thenThrow(exception);
		BeaconAlleleRequest request = BeaconAlleleRequest.create("1", 100L, "A", "T");

		try
		{
			beaconQueryService.query("beacon", request);
		}
		catch (BeaconException e)
		{
			BeaconException beaconException = new NestedBeaconException(BEACON_ID, request);
			assertEquals(e.getMessage(), beaconException.getMessage());
		}
	}
}
