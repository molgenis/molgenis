package org.molgenis.auth;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.XREF;

@Component
public class UserAuthorityMetaData extends DefaultEntityMetaData {
    public static final String ID = "id";
    public static final String MOLGENISUSER = "molgenisUser";

    String id;

    public UserAuthorityMetaData() {
        super("UserAuthority");
        addAttribute(ID).setAuto(true).setVisible(false)
                .setDescription("").setIdAttribute(true).setNillable(false).setLabelAttribute(true);
        addAttribute(MOLGENISUSER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData()).setAggregateable(true).setDescription("").setNillable(false);
        addAttribute(AuthorityMetaData.ROLE).setDescription("").setNillable(false);
    }
}
