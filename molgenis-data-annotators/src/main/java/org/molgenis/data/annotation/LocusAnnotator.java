package org.molgenis.data.annotation;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.RepositoryAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;

/**
 * Created by jvelde on 2/13/14.
 */
public abstract class LocusAnnotator implements RepositoryAnnotator {

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
     * TODO: data type check
     * @param inputMetaData
     * @return
     */
    @Override
    public Boolean canAnnotate(EntityMetaData inputMetaData)
    {
        boolean canAnnotate = true;
        Iterable<AttributeMetaData> inputAttributes = getInputMetaData().getAttributes();
        for(AttributeMetaData attribute : inputAttributes){
            if(inputMetaData.getAttribute(attribute.getName()) == null){
                //all attributes from the inputmetadata must be present to annotate.
                canAnnotate = false;
            }
        }
        return canAnnotate;
    }



}
