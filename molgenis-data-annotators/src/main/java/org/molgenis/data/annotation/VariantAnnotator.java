package org.molgenis.data.annotation;

import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.vcf.VcfRepository;

public abstract class VariantAnnotator extends LocusAnnotator
{
	@Override
	public List<AttributeMetaData> getInputMetaData()
	{
		List<AttributeMetaData> metadata = new ArrayList<>();

		metadata.add(VcfRepository.CHROM_META);
		metadata.add(VcfRepository.POS_META);
		metadata.add(VcfRepository.REF_META);
		metadata.add(VcfRepository.ALT_META);

		return metadata;
	}

	@Override
	protected boolean annotationDataExists()
	{
		return true;
	}
}
