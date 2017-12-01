package org.molgenis.beacon.service.impl;

import org.molgenis.beacon.config.Beacon;
import org.molgenis.beacon.controller.model.BeaconResponse;
import org.molgenis.beacon.service.BeaconInfoService;
import org.molgenis.data.DataService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.molgenis.beacon.config.BeaconMetadata.BEACON;

@Component
public class BeaconInfoServiceImpl implements BeaconInfoService
{
	private final DataService dataService;

	public BeaconInfoServiceImpl(DataService dataService)
	{
		this.dataService = Objects.requireNonNull(dataService);
	}

	@Override
	public List<BeaconResponse> getAvailableBeacons()
	{
		return dataService.findAll(BEACON, Beacon.class).map(Beacon::toBeaconResponse).collect(Collectors.toList());
	}

	@Override
	public BeaconResponse info(String beaconId)
	{
		return dataService.findOneById(BEACON, beaconId, Beacon.class).toBeaconResponse();
	}
}
