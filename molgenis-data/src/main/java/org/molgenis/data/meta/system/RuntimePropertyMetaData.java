package org.molgenis.data.meta.system;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

@Component
public class RuntimePropertyMetaData extends DefaultEntityMetaData {
    public static final String ID = "id";
    public static final String NAME = "Name";
    public static final String VALUE = "Value";
    public RuntimePropertyMetaData() {
        super("runtimeProperty");
        addAttribute(ID).setAuto(true).setVisible(false)
                .setDescription("automatically generated internal id, only for internal use.");
        addAttribute(NAME).setUnique(true);
        addAttribute(VALUE).setNillable(false);
    }
}
