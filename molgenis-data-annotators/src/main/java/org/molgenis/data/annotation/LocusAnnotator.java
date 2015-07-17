package org.molgenis.data.annotation;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.vcf.VcfRepository;

/**
 * Created by jvelde on 2/13/14.
 */
public abstract class LocusAnnotator extends AbstractRepositoryEntityAnnotator
{
	// TODO: needs genome build and possible organism !?

	@Override
	public List<AttributeMetaData> getInputMetaData()
	{
		List<AttributeMetaData> metadata = new ArrayList<>();
		metadata.add(VcfRepository.CHROM_META);
		metadata.add(VcfRepository.POS_META);

		return metadata;
	}

}
