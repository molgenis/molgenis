package org.molgenis.data.annotation;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;

import java.util.ArrayList;
import java.util.List;

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
