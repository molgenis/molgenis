package org.molgenis.auth;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.XREF;

@Component
public class GroupAuthorityMetaData extends DefaultEntityMetaData {
    public static final String ID = "id";
    public static final String MOLGENISGROUP = "molgenisGroup";

    public GroupAuthorityMetaData() {
        super("GroupAuthority");
        addAttribute(ID).setAuto(true).setVisible(false)
                .setDescription("").setIdAttribute(true).setNillable(false).setLabelAttribute(true);
        addAttribute(MOLGENISGROUP).setDataType(XREF).setRefEntity(new MolgenisGroupMetaData()).setAggregateable(true).setDescription("").setNillable(false);
        addAttribute(AuthorityMetaData.ROLE).setDescription("").setNillable(false);
    }
}
