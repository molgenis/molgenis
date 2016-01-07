package org.molgenis.data.meta.system;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.XREF;
import static org.molgenis.MolgenisFieldTypes.TEXT;

@Component
public class MolgenisTokenMetaData extends DefaultEntityMetaData {
    public static final String ID = "id";
    public static final String MOLGENISUSER = "molgenisUser";
    public static final String TOKEN = "token";
    public static final String EXPIRATIONDATE = "expirationDate";
    public static final String CREATIONDATE = "creationDate";
    public static final String DESCRIPTION = "description";

    public MolgenisTokenMetaData() {
        super("molgenisToken");
        addAttribute(ID).setAuto(true).setVisible(false)
                .setDescription("automatically generated internal id, only for internal use.");
        addAttribute(MOLGENISUSER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData());
        addAttribute(TOKEN).setLabel("Token").setUnique(true);
        addAttribute(EXPIRATIONDATE).setDataType(DATETIME).setLabel("Expiration date").setNillable(true).setDescription("When expiration date is null it will never expire");
        addAttribute(CREATIONDATE).setDataType(DATETIME).setLabel("Creation date").setAuto(true).setReadOnly(true);
        addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description").setNillable(true);

    }
}
