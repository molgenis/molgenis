package org.molgenis.data.annotation;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;

public abstract class VariantAnnotator extends LocusAnnotator
{

	public static final String QUAL = VcfRepository.QUAL;
	public static final String FILTER = VcfRepository.FILTER;
	public static final String SAMPLES = VcfRepository.SAMPLES;

	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(VcfRepository.CHROM_META);
		metadata.addAttributeMetaData(VcfRepository.POS_META);
		metadata.addAttributeMetaData(VcfRepository.REF_META);
		metadata.addAttributeMetaData(VcfRepository.POS_META);

		return metadata;
	}
}
