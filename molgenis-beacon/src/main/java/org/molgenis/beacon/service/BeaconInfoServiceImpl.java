package org.molgenis.beacon.service;

import org.molgenis.beacon.config.Beacon;
import org.molgenis.data.DataService;

import java.util.Objects;

public class BeaconInfoServiceImpl implements BeaconInfoService
{
	private DataService dataService;

	public BeaconInfoServiceImpl(DataService dataservice)
	{
		this.dataService = Objects.requireNonNull(dataservice);
	}

	@Override
	public Beacon info()
	{

		return null;
	}
}
