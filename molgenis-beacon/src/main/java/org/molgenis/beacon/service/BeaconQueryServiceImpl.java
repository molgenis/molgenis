package org.molgenis.beacon.service;

import org.molgenis.beacon.config.Beacon;
import org.molgenis.beacon.config.BeaconMetadata;
import org.molgenis.beacon.controller.model.BeaconAlleleRequest;
import org.molgenis.beacon.controller.model.BeaconAlleleResponse;
import org.molgenis.beacon.controller.model.BeaconError;
import org.molgenis.data.DataService;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata;
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
		catch (Exception e)
		{
			// FIXME What should the error code be?
			return BeaconAlleleResponse.create(beaconId, null, BeaconError.create(1, e.getMessage()),
					BeaconAlleleRequest.create(referenceName, start, referenceBases, alternateBases));
		}
	}

	private boolean searchBeaconForQueryString(String referenceName, Long start, String referenceBases,
			String alternateBases, String beaconId)
	{
		Beacon beacon = dataService.findOneById(BeaconMetadata.BEACON, beaconId, Beacon.class);
		for (EntityType entityType : beacon.getDataSets())
		{
			boolean exists = queryBeaconDataset(entityType, referenceName, start, referenceBases, alternateBases);
			if (exists)
			{
				return true;
			}
		}
		return false;
	}

	private boolean queryBeaconDataset(EntityType entityType, String referenceName, Long start, String referenceBases,
			String alternateBases)
	{
		/* Use the count() method to determine if a variation exists */
		return dataService.count(entityType.getId(),
				new QueryImpl<>().eq(GenomeBrowserAttributesMetadata.CHROM, referenceName)
								 .and()
								 .eq(GenomeBrowserAttributesMetadata.POS, start)
								 .and()
								 .eq(GenomeBrowserAttributesMetadata.REF, referenceBases)
								 .and()
								 .eq(GenomeBrowserAttributesMetadata.ALT, alternateBases)) > 0;
	}
}
