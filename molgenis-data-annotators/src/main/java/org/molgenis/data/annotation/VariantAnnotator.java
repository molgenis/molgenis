package org.molgenis.data.annotation;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;

public abstract class VariantAnnotator extends LocusAnnotator
{

	public static final String REFERENCE = "REF";
	public static final String ALTERNATIVE = "ALT";

	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		DefaultAttributeMetaData chrom = new DefaultAttributeMetaData(CHROMOSOME,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		chrom.setDescription("The chromosome on which the variant is observed");
		DefaultAttributeMetaData pos = new DefaultAttributeMetaData(POSITION, MolgenisFieldTypes.FieldTypeEnum.LONG);
		pos.setDescription("The position on the chromosome which the variant is observed");
		DefaultAttributeMetaData ref = new DefaultAttributeMetaData(REFERENCE, MolgenisFieldTypes.FieldTypeEnum.STRING);
		ref.setDescription("The reference allele");
		DefaultAttributeMetaData alt = new DefaultAttributeMetaData(ALTERNATIVE,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		alt.setDescription("The alternative allele observed");

		metadata.addAttributeMetaData(chrom);
		metadata.addAttributeMetaData(pos);
		metadata.addAttributeMetaData(ref);
		metadata.addAttributeMetaData(alt);

		return metadata;
	}
}
