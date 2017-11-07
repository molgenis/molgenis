package org.molgenis.beacon.service;

import org.molgenis.data.DataService;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.data.vcf.model.VcfAttributes;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
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
	public List<String> listDatasets()
	{
		return newArrayList("base_beacon_dataset_one", "base_beacon_dataset_two");
	}

	@Override
	public boolean query(String chromosome, Long position, String reference, String allele, String entityTypeID)
	{
		/* Use the count() method to determine if a variation exists */
		return dataService.count(entityTypeID, new QueryImpl<>().eq(VcfAttributes.CHROM, chromosome)
																.and()
																.eq(VcfAttributes.POS, position)
																.and()
																.eq(VcfAttributes.REF, reference)
																.and()
																.eq(VcfAttributes.ALT, allele)) > 0;
	}
}
