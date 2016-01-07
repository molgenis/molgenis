package org.molgenis.data.meta.system;

import org.molgenis.data.support.DefaultEntityMetaData;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.XREF;

public abstract class AuthorityMetaData extends DefaultEntityMetaData {

    public static final String ROLE = "role";

    public AuthorityMetaData() {
        super("authority");
        setAbstract(true);
        addAttribute(ROLE);
    }

    /*public static final String ENTITY_NAME = "MappingProject";
    public static final String IDENTIFIER = "identifier";
    public static final String NAME = "name";
    public static final String OWNER = "owner";
    public static final String MAPPINGTARGETS = "mappingtargets";

    public MappingProjectMetaData()
    {
        super(ENTITY_NAME);

        addAttribute(IDENTIFIER).setIdAttribute(true).setNillable(false).setDataType(STRING);
        addAttribute(NAME).setNillable(false);
        addAttribute(OWNER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData());
        addAttribute(MAPPINGTARGETS).setDataType(MREF).setRefEntity(MappingTargetRepositoryImpl.META_DATA);
    }


    String role;
    </entity>
    <entity name="Authority" abstract="true" system="true" xref_lookup="name">
    <field name="role" type="string"/>
    </entity>  */

}
