package org.molgenis.beacon.service.impl;

import org.molgenis.beacon.config.Beacon;
import org.molgenis.beacon.config.BeaconDataset;
import org.molgenis.beacon.config.BeaconMetadata;
import org.molgenis.beacon.controller.model.BeaconAlleleRequest;
import org.molgenis.beacon.controller.model.BeaconAlleleResponse;
import org.molgenis.beacon.controller.model.exceptions.NestedBeaconException;
import org.molgenis.beacon.controller.model.exceptions.UnknownBeaconException;
import org.molgenis.beacon.service.BeaconQueryService;
import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class BeaconQueryServiceImpl implements BeaconQueryService
{
	private static final Logger LOG = LoggerFactory.getLogger(BeaconQueryServiceImpl.class);
	private final DataService dataService;

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
			// TODO create @ControllerAdvice BeaconExceptionHandler to handle beacon exceptions returning proper responses.
			// Till then, let's log the exception here instead of quietly eating it up.
			LOG.error("An exception occurred while querying for beacon", e);
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
			for (BeaconDataset beaconDataset : beacon.getDataSets())
			{
				exists = queryBeaconDataset(beaconDataset, referenceName, start, referenceBases, alternateBases);
			}
		}
		else
		{
			throw new UnknownBeaconException(beaconId,
					BeaconAlleleRequest.create(referenceName, start, referenceBases, alternateBases));
		}
		return exists;
	}

	private boolean queryBeaconDataset(BeaconDataset beaconDataset, String referenceName, Long start,
			String referenceBases, String alternateBases)
	{
		/* Use a count query to determine if a variation exists */

		String alt = beaconDataset.getGenomeBrowserAttributes().getAlt();
		String ref = beaconDataset.getGenomeBrowserAttributes().getRef();

		if (alt == null || alt.isEmpty())
		{
			alt = "ALT";
		}

		if (ref == null || ref.isEmpty())
		{
			ref = "REF";
		}

		return dataService.count(beaconDataset.getDatasetEntityType().getId(),
				new QueryImpl<>().eq(beaconDataset.getGenomeBrowserAttributes().getChrom(), referenceName)
								 .and()
								 .eq(beaconDataset.getGenomeBrowserAttributes().getPos(), start)
								 .and()
								 .eq(ref, referenceBases)
								 .and()
								 .eq(alt, alternateBases)) > 0;
	}
}
