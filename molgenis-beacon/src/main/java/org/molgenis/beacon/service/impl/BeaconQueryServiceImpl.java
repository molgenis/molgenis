package org.molgenis.beacon.service.impl;

import org.molgenis.beacon.config.Beacon;
import org.molgenis.beacon.config.BeaconMetadata;
import org.molgenis.beacon.controller.model.BeaconAlleleRequest;
import org.molgenis.beacon.controller.model.BeaconAlleleResponse;
import org.molgenis.beacon.model.exceptions.NestedBeaconException;
import org.molgenis.beacon.model.exceptions.UnknownBeaconException;
import org.molgenis.beacon.service.BeaconQueryService;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class BeaconQueryServiceImpl implements BeaconQueryService
{
	private DataService dataService;

	public BeaconQueryServiceImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public BeaconAlleleResponse query(String beaconId, BeaconAlleleRequest request)
	{
		return query(request.getReferenceName(), request.getStart(), request.getReferenceBases(),
				request.getAlternateBases(), beaconId);
	}

	@Override
	public BeaconAlleleResponse query(String referenceName, Long start, String referenceBases, String alternateBases,
			String beaconId)
	{
		try
		{
			boolean exists = searchBeaconForQueryString(referenceName, start, referenceBases, alternateBases, beaconId);
			return BeaconAlleleResponse.create(beaconId, exists, null,
					BeaconAlleleRequest.create(referenceName, start, referenceBases, alternateBases));
		}
		catch (UnknownBeaconException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			throw new NestedBeaconException(beaconId,
					BeaconAlleleRequest.create(referenceName, start, referenceBases, alternateBases));
		}
	}

	private boolean searchBeaconForQueryString(String referenceName, Long start, String referenceBases,
			String alternateBases, String beaconId)
	{
		boolean exists = false;
		Beacon beacon = dataService.findOneById(BeaconMetadata.BEACON, beaconId, Beacon.class);
		if (beacon != null)
		{
			for (EntityType entityType : beacon.getDataSets())
			{
				exists = queryBeaconDataset(entityType, referenceName, start, referenceBases, alternateBases);
			}
		}
		else
		{
			throw new UnknownBeaconException(beaconId,
					BeaconAlleleRequest.create(referenceName, start, referenceBases, alternateBases));
		}
		return exists;
	}

	private boolean queryBeaconDataset(EntityType entityType, String referenceName, Long start, String referenceBases,
			String alternateBases)
	{
		/* Use a count query to determine if a variation exists */
		return dataService.count(entityType.getId(),
				// FIXME How to use GenomeBrowserAttributes?
				new QueryImpl<>().eq("#CHROM", referenceName)
								 .and()
								 .eq("POS", start)
								 .and()
								 .eq("REF", referenceBases)
								 .and()
								 .eq("ALT", alternateBases)) > 0;
	}
}
