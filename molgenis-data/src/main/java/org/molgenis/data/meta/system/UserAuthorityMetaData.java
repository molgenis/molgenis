package org.molgenis.data.meta.system;

import org.molgenis.data.support.DefaultEntityMetaData;

import static org.molgenis.MolgenisFieldTypes.XREF;

public class UserAuthorityMetaData extends DefaultEntityMetaData {
    public static final String ID = "id";
    public static final String MOLGENISUSER = "molgenisUser";

    String id;

    public UserAuthorityMetaData() {
        super("userAuthority");
        addAttribute(ID).setAuto(true).setVisible(false)
                .setDescription("automatically generated internal id, only for internal use.");
        addAttribute(MOLGENISUSER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData());
    }
}
