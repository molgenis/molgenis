package org.molgenis.data.meta.system;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.XREF;

@Component
public class MolgenisGroupMemberMetaData extends DefaultEntityMetaData {
    public static final String ID = "id";
    public static final String MOLGENISUSER = "molgenisUser";
    public static final String MOLGENISGROUP = "molgenisGroup";

    public MolgenisGroupMemberMetaData() {
        super("molgenisGroupMember");
        addAttribute(ID).setAuto(true).setVisible(false)
                .setDescription("automatically generated internal id, only for internal use.");
        addAttribute(MOLGENISUSER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData());
        addAttribute(MOLGENISGROUP).setDataType(XREF).setRefEntity(new MolgenisGroupMetaData());;
    }
}
