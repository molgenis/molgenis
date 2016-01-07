package org.molgenis.data.meta.system;

import org.molgenis.data.support.DefaultEntityMetaData;
import static org.molgenis.MolgenisFieldTypes.XREF;

public class GroupAuthorityMetaData extends DefaultEntityMetaData {
    public static final String ID = "id";
    public static final String MOLGENISGROUP = "molgenisGroup";

    public GroupAuthorityMetaData() {
        super("groupAuthority");
        addAttribute(ID).setAuto(true).setVisible(false)
                .setDescription("automatically generated internal id, only for internal use.");
        addAttribute(MOLGENISGROUP).setDataType(XREF).setRefEntity(new MolgenisGroupMetaData());
    }
}
