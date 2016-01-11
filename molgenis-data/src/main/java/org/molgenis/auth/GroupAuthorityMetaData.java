package org.molgenis.auth;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.XREF;

@Component
public class GroupAuthorityMetaData extends DefaultEntityMetaData {
    public static final String ID = "id";
    public static final String MOLGENISGROUP = "molgenisGroup";

    public GroupAuthorityMetaData() {
        super("groupAuthority");
        addAttribute(ID).setAuto(true).setVisible(false)
                .setDescription("automatically generated internal id, only for internal use.").setIdAttribute(true).setNillable(false);
        addAttribute(MOLGENISGROUP).setDataType(XREF).setRefEntity(new MolgenisGroupMetaData());
    }
}
