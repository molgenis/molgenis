package org.molgenis.auth;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.XREF;

@Component
public class MolgenisGroupMemberMetaData extends DefaultEntityMetaData {
    public static final String ID = "id";
    public static final String MOLGENISUSER = "molgenisUser";
    public static final String MOLGENISGROUP = "molgenisGroup";

    public MolgenisGroupMemberMetaData() {
        super("MolgenisGroupMember");
        addAttribute(ID).setAuto(true).setVisible(false)
                .setDescription("").setIdAttribute(true).setNillable(false).setLabelAttribute(true);
        addAttribute(MOLGENISUSER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData()).setAggregateable(true).setDescription("").setNillable(false);
        addAttribute(MOLGENISGROUP).setDataType(XREF).setRefEntity(new MolgenisGroupMetaData()).setAggregateable(true).setDescription("").setNillable(false);
    }
}
