package org.molgenis.data.annotation;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;

public abstract class VariantAnnotator extends LocusAnnotator
{
    @Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(VcfRepository.CHROM_META);
		metadata.addAttributeMetaData(VcfRepository.POS_META);
		metadata.addAttributeMetaData(VcfRepository.REF_META);
		metadata.addAttributeMetaData(VcfRepository.ALT_META);

		return metadata;
	}

    @Override
    protected boolean annotationDataExists()
    {
        return true;
    }
}
