package org.molgenis.beacon.service;

import org.mockito.Mock;
import org.molgenis.beacon.config.Beacon;
import org.molgenis.beacon.controller.model.BeaconDatasetResponse;
import org.molgenis.beacon.controller.model.BeaconResponse;
import org.molgenis.beacon.service.impl.BeaconInfoServiceImpl;
import org.molgenis.data.DataService;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.beacon.config.BeaconMetadata.BEACON;
import static org.testng.Assert.assertEquals;

public class BeaconInfoServiceTest
{
	private BeaconInfoService beaconInfoService;

	@Mock
	private DataService dataService;

	@BeforeMethod
	public void beforeMethod()
	{
		initMocks(this);
		beaconInfoService = new BeaconInfoServiceImpl(dataService);
	}

	@Test
	public void getAvailableBeaconsTest()
	{
		List<BeaconDatasetResponse> beaconDatasets = newArrayList(BeaconDatasetResponse.create("dataset", "DATA", ""));
		BeaconResponse beaconResponse = BeaconResponse.create("beacon", "My Beacon", "0.3.0", null, "", "", "",
				beaconDatasets);

		Beacon beacon = mock(Beacon.class);
		when(beacon.toBeaconResponse()).thenReturn(beaconResponse);

		when(dataService.findAll(BEACON, Beacon.class)).thenReturn(Stream.of(beacon));

		List<BeaconResponse> expectedBeaconList = newArrayList(beaconResponse);
		assertEquals(beaconInfoService.getAvailableBeacons(), expectedBeaconList);
	}

	@Test
	public void infoTest()
	{
		List<BeaconDatasetResponse> beaconDatasets = newArrayList(BeaconDatasetResponse.create("dataset", "DATA", ""));
		BeaconResponse beaconResponse = BeaconResponse.create("beacon", "My Beacon", "0.3.0", null, "", "", "",
				beaconDatasets);

		Beacon beacon = mock(Beacon.class);
		when(beacon.toBeaconResponse()).thenReturn(beaconResponse);

		when(dataService.findOneById(BEACON, "beacon", Beacon.class)).thenReturn(beacon);

		assertEquals(beaconInfoService.info("beacon"), beaconResponse);
	}
}
