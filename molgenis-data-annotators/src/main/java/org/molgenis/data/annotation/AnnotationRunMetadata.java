package org.molgenis.data.annotation;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.fieldtypes.FieldType;

import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;

public class AnnotationRunMetadata extends DefaultEntityMetaData
{
    public static final String ENTITY_NAME = "AnnotationRun";
    public static final EntityMetaData META = new AnnotationRunMetadata();

    public AnnotationRunMetadata()
    {
        super(ENTITY_NAME);

        addAttribute(AnnotationRun.ID, ROLE_ID);
        addAttribute(AnnotationRun.USER);
        addAttribute(AnnotationRun.STATUS);
        addAttribute(AnnotationRun.MESSAGE).setDataType(MolgenisFieldTypes.TEXT);
        addAttribute(AnnotationRun.ENTITY);
    }
}
