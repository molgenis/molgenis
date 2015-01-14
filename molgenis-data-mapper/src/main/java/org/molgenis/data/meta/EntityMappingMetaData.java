package org.molgenis.data.meta;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.XREF;

import org.molgenis.data.repository.EntityMappingRepository;
import org.molgenis.data.support.DefaultEntityMetaData;

public class EntityMappingMetaData extends DefaultEntityMetaData
{
    public static final String ENTITY_NAME = "attributes";
    public static final String IDENTIFIER = "identifier";
    public static final String SOURCEENTITYMETADATA = "sourceEntityMetaData";
    public static final String TARGETENTITYMETADATA = "targetEntityMetaData";
    public static final String ENTITYMAPPINGS = "entityMappings";

    public EntityMappingMetaData()
    {
        super(ENTITY_NAME);

        addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setDataType(STRING);
		addAttribute(SOURCEENTITYMETADATA).setDataType(XREF).setRefEntity(EntityMetaDataRepository.META_DATA);
		addAttribute(TARGETENTITYMETADATA).setDataType(XREF).setRefEntity(EntityMetaDataRepository.META_DATA);
		addAttribute(ENTITYMAPPINGS).setDataType(MREF).setRefEntity(EntityMappingRepository.META_DATA);
    }
}
