package org.molgenis.beacon.service;

import com.google.common.collect.Lists;
import org.mockito.Mock;
import org.molgenis.beacon.config.Beacon;
import org.molgenis.beacon.config.BeaconMetadata;
import org.molgenis.beacon.controller.model.BeaconAlleleRequest;
import org.molgenis.beacon.controller.model.BeaconAlleleResponse;
import org.molgenis.beacon.controller.model.BeaconError;
import org.molgenis.beacon.service.impl.BeaconQueryServiceImpl;
import org.molgenis.data.DataService;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata.*;
import static org.testng.Assert.assertEquals;

public class BeaconQueryServiceTest
{
	private BeaconQueryService beaconQueryService;

	@Mock
	private DataService dataService;

	private Query query;

	private EntityType dataset1;
	private EntityType dataset2;

	private static final String BEACON_ID = "beacon";

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);

		dataset1 = mock(EntityType.class);
		when(dataset1.getId()).thenReturn("dataset1");

		dataset2 = mock(EntityType.class);
		when(dataset2.getId()).thenReturn("dataset2");

		query = new QueryImpl<>().eq(CHROM, "1").and().eq(POS, 100L).and().eq(REF, "A").and().eq(ALT, "T");

		beaconQueryService = new BeaconQueryServiceImpl(dataService);
	}

	@Test
	public void getQueryExistsTest()
	{
		Beacon beacon = mock(Beacon.class);
		when(beacon.getDataSets()).thenReturn(Lists.newArrayList(dataset1, dataset2));

		when(dataService.findOneById(BeaconMetadata.BEACON, BEACON_ID, Beacon.class)).thenReturn(beacon);

		doReturn(0L).when(dataService).count("dataset1", query);
		doReturn(1L).when(dataService).count("dataset2", query);

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

		doReturn(0L).when(dataService).count("dataset1", query);
		doReturn(1L).when(dataService).count("dataset2", query);

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

		doReturn(0L).when(dataService).count("dataset1", query);
		doReturn(0L).when(dataService).count("dataset2", query);

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

		BeaconError error = BeaconError.create(1, "Error test");
		BeaconAlleleRequest request = BeaconAlleleRequest.create("1", 100L, "A", "T");

		BeaconAlleleResponse actualResponse = beaconQueryService.query("beacon", request);
		BeaconAlleleResponse expectedResponse = BeaconAlleleResponse.create(BEACON_ID, null, error, request);

		assertEquals(actualResponse, expectedResponse);
	}
}
