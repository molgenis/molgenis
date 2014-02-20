package org.molgenis.data.annotation;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.RepositoryAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

/**
 * Created by jvelde on 2/13/14.
 */
public abstract class LocusAnnotator implements RepositoryAnnotator, ApplicationListener<ContextRefreshedEvent> {

    protected static final String CHROMOSOME = "chrom";
    protected static final String POSITION = "pos";

    /**
     * TODO: needs genome build and possible organism !?
     */

    @Override
    public EntityMetaData getInputMetaData()
    {
        DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName());

        metadata.addAttributeMetaData(new DefaultAttributeMetaData(CHROMOSOME, MolgenisFieldTypes.FieldTypeEnum.STRING));
        metadata.addAttributeMetaData(new DefaultAttributeMetaData(POSITION, MolgenisFieldTypes.FieldTypeEnum.LONG));

        return metadata;
    }

    /**
     * @param inputMetaData
     * @return
     */
    public boolean canAnnotate(EntityMetaData inputMetaData)
	{
		boolean canAnnotate = true;
		Iterable<AttributeMetaData> inputAttributes = getInputMetaData().getAttributes();

		for (AttributeMetaData attribute : inputAttributes)
		{
			if (inputMetaData.getAttribute(attribute.getName()) == null) canAnnotate = false;
			else if (!inputMetaData.getAttribute(attribute.getName()).getDataType().equals(attribute.getDataType()))
			{

				canAnnotate = false;
			}
		}
		return canAnnotate;
	}



}
