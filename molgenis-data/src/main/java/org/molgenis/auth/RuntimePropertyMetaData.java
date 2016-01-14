package org.molgenis.auth;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import static org.molgenis.MolgenisFieldTypes.TEXT;

@Component
public class RuntimePropertyMetaData extends DefaultEntityMetaData {
    public static final String ID = "id";
    public static final String NAME = "Name";
    public static final String VALUE = "Value";
    public RuntimePropertyMetaData() {
        super("RuntimeProperty");
        addAttribute(ID).setAuto(true).setVisible(false)
                .setDescription("automatically generated internal id, only for internal use.").setIdAttribute(true).setNillable(false);
        addAttribute(NAME).setUnique(true).setDescription("").setLabelAttribute(true).setNillable(false);
        addAttribute(VALUE).setNillable(false).setDescription("").setDataType(TEXT);
    }
}
