package org.molgenis.beacon.service;

import static com.google.common.collect.Lists.newArrayList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.molgenis.beacon.config.BeaconMetadata.BEACON;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.molgenis.beacon.config.Beacon;
import org.molgenis.beacon.controller.model.BeaconDatasetResponse;
import org.molgenis.beacon.controller.model.BeaconResponse;
import org.molgenis.beacon.service.impl.BeaconInfoServiceImpl;
import org.molgenis.data.DataService;
import org.molgenis.data.UnknownEntityException;

class BeaconInfoServiceImplTest {
  private BeaconInfoService beaconInfoService;

  @Mock private DataService dataService;

  @BeforeEach
  void beforeMethod() {
    initMocks(this);
    beaconInfoService = new BeaconInfoServiceImpl(dataService);
  }

  @Test
  void getAvailableBeaconsTest() {
    List<BeaconDatasetResponse> beaconDatasets =
        newArrayList(BeaconDatasetResponse.create("dataset", "DATA", ""));
    BeaconResponse beaconResponse =
        BeaconResponse.create("beacon", "My Beacon", "0.3.0", null, "", "", "", beaconDatasets);

    Beacon beacon = mock(Beacon.class);
    when(beacon.toBeaconResponse()).thenReturn(beaconResponse);

    when(dataService.findAll(BEACON, Beacon.class)).thenReturn(Stream.of(beacon));

    List<BeaconResponse> expectedBeaconList = newArrayList(beaconResponse);
    assertEquals(expectedBeaconList, beaconInfoService.getAvailableBeacons());
  }

  @Test
  void infoTest() {
    List<BeaconDatasetResponse> beaconDatasets =
        newArrayList(BeaconDatasetResponse.create("dataset", "DATA", ""));
    BeaconResponse beaconResponse =
        BeaconResponse.create("beacon", "My Beacon", "0.3.0", null, "", "", "", beaconDatasets);

    Beacon beacon = mock(Beacon.class);
    when(beacon.toBeaconResponse()).thenReturn(beaconResponse);

    when(dataService.findOneById(BEACON, "beacon", Beacon.class)).thenReturn(beacon);

    assertEquals(beaconResponse, beaconInfoService.info("beacon"));
  }

  @Test
  void testInfoUnknownBeacon() {
    assertThrows(UnknownEntityException.class, () -> beaconInfoService.info("unknownBeacon"));
  }
}
