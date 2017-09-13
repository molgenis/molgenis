package org.molgenis.bootstrap.populate;

import org.molgenis.data.DataService;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributesFactory;
import org.molgenis.genomebrowser.meta.GenomeBrowserAttributesMetadata;
import org.springframework.stereotype.Component;

import static java.util.Objects.requireNonNull;

@Component
public class GenomeBrowserAttributesPopulator
{
	private final DataService dataService;
	private final GenomeBrowserAttributesFactory gbaFactory;

	public GenomeBrowserAttributesPopulator(DataService dataService, GenomeBrowserAttributesFactory gbaFactory)
	{
		this.gbaFactory = requireNonNull(gbaFactory);
		this.dataService = requireNonNull(dataService);
	}

	public void populate()
	{
		dataService.add(GenomeBrowserAttributesMetadata.GENOMEBROWSERATTRIBUTES,
				gbaFactory.create("simple1", true, 0, "start", "chr", null, null, null));
		dataService.add(GenomeBrowserAttributesMetadata.GENOMEBROWSERATTRIBUTES,
				gbaFactory.create("VCF", true, 1, "POS", "#CHROM", "REF", "ALT", null));
		dataService.add(GenomeBrowserAttributesMetadata.GENOMEBROWSERATTRIBUTES,
				gbaFactory.create("full", true, 2, "start", "chr", "ref", "alt", "stop"));
		dataService.add(GenomeBrowserAttributesMetadata.GENOMEBROWSERATTRIBUTES,
				gbaFactory.create("simple2", true, 3, "POS", "#CHROM", null, null, null));
	}
}
