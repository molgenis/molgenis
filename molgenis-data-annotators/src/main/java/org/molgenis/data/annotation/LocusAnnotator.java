package org.molgenis.data.annotation;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
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

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CHROMOSOME, MolgenisFieldTypes.FieldTypeEnum.STRING));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(POSITION, MolgenisFieldTypes.FieldTypeEnum.LONG));

		return metadata;
	}
}
