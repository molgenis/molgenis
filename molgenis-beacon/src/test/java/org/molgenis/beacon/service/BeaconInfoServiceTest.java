package org.molgenis.beacon.service;

import org.mockito.Mock;
import org.molgenis.beacon.config.Beacon;
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
		Beacon beacon = mock(Beacon.class);
		when(dataService.findAll(BEACON, Beacon.class)).thenReturn(Stream.of(beacon));

		List<Beacon> expectedBeaconList = newArrayList(beacon);
		assertEquals(beaconInfoService.getAvailableBeacons(), expectedBeaconList);
	}

	@Test
	public void infoTest()
	{
		Beacon beacon = mock(Beacon.class);
		when(dataService.findOneById(BEACON, "beacon", Beacon.class)).thenReturn(beacon);

		assertEquals(beaconInfoService.info("beacon"), beacon);
	}
}
