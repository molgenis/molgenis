package org.molgenis.data.annotation;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Created by jvelde on 2/13/14.
 */
public abstract class LocusAnnotator extends AbstractRepositoryAnnotator implements RepositoryAnnotator,
		ApplicationListener<ContextRefreshedEvent>
{

	public static final String CHROMOSOME = "#CHROM";
	public static final String POSITION = "POS";

	// TODO: needs genome build and possible organism !?

	@Override
	public EntityMetaData getInputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);
		DefaultAttributeMetaData attr1 = new DefaultAttributeMetaData(CHROMOSOME,
				MolgenisFieldTypes.FieldTypeEnum.STRING);
		attr1.setDescription("The chromosome on which the variant is observed");
		DefaultAttributeMetaData attr2 = new DefaultAttributeMetaData(POSITION, MolgenisFieldTypes.FieldTypeEnum.LONG);
		attr2.setDescription("The position on the chromosome which the variant is observed");
		metadata.addAttributeMetaData(attr1);
		metadata.addAttributeMetaData(attr2);

		return metadata;
	}
}
