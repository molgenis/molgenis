package org.molgenis.beacon.service;

import org.molgenis.beacon.config.Beacon;
import org.molgenis.data.DataService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.molgenis.beacon.config.BeaconMetadata.BEACON;

@Component
public class BeaconInfoServiceImpl implements BeaconInfoService
{
	private DataService dataService;

	public BeaconInfoServiceImpl(DataService dataservice)
	{
		this.dataService = Objects.requireNonNull(dataservice);
	}

	@Override
	public List<Beacon> getAvailableBeacons()
	{
		return dataService.findAll(BEACON, Beacon.class).collect(Collectors.toList());
	}

	@Override
	public Beacon info(String beaconId)
	{
		return dataService.findOneById(BEACON, beaconId, Beacon.class);
	}
}
