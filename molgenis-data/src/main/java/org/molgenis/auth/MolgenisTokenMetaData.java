package org.molgenis.auth;

import org.molgenis.auth.MolgenisUserMetaData;
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
        super("MolgenisToken");
        addAttribute(ID).setAuto(true).setVisible(false)
                .setDescription("").setIdAttribute(true).setNillable(false);
        addAttribute(MOLGENISUSER).setDataType(XREF).setRefEntity(new MolgenisUserMetaData()).setAggregateable(true).setDescription("").setNillable(false);
        addAttribute(TOKEN).setLabel("Token").setUnique(true).setDescription("").setLabelAttribute(true).setNillable(false);
        addAttribute(EXPIRATIONDATE).setDataType(DATETIME).setLabel("Expiration date").setNillable(true).setDescription("When expiration date is null it will never expire");
        addAttribute(CREATIONDATE).setDataType(DATETIME).setLabel("Creation date").setAuto(true).setReadOnly(true).setDescription("").setNillable(false);
        addAttribute(DESCRIPTION).setDataType(TEXT).setLabel("Description").setNillable(true).setDescription("");

    }
}
