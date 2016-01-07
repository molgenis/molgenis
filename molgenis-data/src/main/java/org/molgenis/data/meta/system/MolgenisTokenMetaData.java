package org.molgenis.data.meta.system;

import org.molgenis.data.support.DefaultEntityMetaData;

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
        addAttribute(MOLGENISUSER);
        addAttribute(TOKEN);
        addAttribute(EXPIRATIONDATE);
        addAttribute(CREATIONDATE);
        addAttribute(DESCRIPTION);

    }
    /*<field name="id" type="autoid" hidden="true" />
    <field name="molgenisUser" type="xref" xref_entity="MolgenisUser" />
    <field name="token" label="Token" type="string" />
    <field name="expirationDate" label="Expiration date" type="datetime" optional="true" description="When expiration date is null it will never expire"/>
    <field name="creationDate" label="Creation date" type="datetime" auto="true" readonly="true" />
    <field name="description" label="Description" type="text" nillable="true" />
    <unique fields="token"/>  */
}
