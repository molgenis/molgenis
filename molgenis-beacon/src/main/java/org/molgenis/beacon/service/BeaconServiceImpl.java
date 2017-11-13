package org.molgenis.beacon.service;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata;
import org.springframework.stereotype.Component;


import static java.util.Objects.requireNonNull;

@Component
public class BeaconServiceImpl implements BeaconService
{
	private DataService dataService;

	public BeaconServiceImpl(DataService dataService)
	{
		this.dataService = requireNonNull(dataService);
	}

	@Override
	public boolean query(String chromosome, Long position, String reference, String allele, String entityTypeID)
	{


		/* Use the count() method to determine if a variation exists */
		return dataService.count(entityTypeID, new QueryImpl<>().eq(GenomeBrowserAttributesMetadata.CHROM, chromosome)
																.and()
																.eq(GenomeBrowserAttributesMetadata.POS, position)
																.and()
																.eq(GenomeBrowserAttributesMetadata.REF, reference)
																.and()
																.eq(GenomeBrowserAttributesMetadata.ALT, allele)) > 0;
	}
}
