package org.molgenis.data.meta.system;

import org.molgenis.data.support.DefaultEntityMetaData;
import org.springframework.stereotype.Component;

import java.util.Arrays;

import static org.molgenis.MolgenisFieldTypes.DATETIME;
import static org.molgenis.MolgenisFieldTypes.ENUM;
import static org.molgenis.MolgenisFieldTypes.STRING;
import static org.molgenis.MolgenisFieldTypes.TEXT;
import static org.molgenis.MolgenisFieldTypes.INT;

@Component
public class ImportRunMetaData extends DefaultEntityMetaData {
    public static final String ID = "id";
    public static final String STARTDATE = "startDate";
    public static final String ENDDATE = "endDate";
    public static final String USERNAME = "userName";
    public static final String STATUS = "status";
    public static final String MESSAGE = "message";
    public static final String PROGRESS = "progress";
    public static final String IMPORTEDENTITIES = "importedEntities";

    public static final ImportRunMetaData INSTANCE = new ImportRunMetaData();

    public ImportRunMetaData() {
        super("ImportRun");
        addAttribute(ID).setAuto(true).setVisible(false)
                .setDescription("automatically generated internal id, only for internal use.").setIdAttribute(true).setNillable(false).setLabelAttribute(true);
        addAttribute(STARTDATE).setDataType(DATETIME).setNillable(false).setDescription("");
        addAttribute(ENDDATE).setDataType(DATETIME).setNillable(true).setDescription("");
        addAttribute(USERNAME).setNillable(false).setDescription("");
        addAttribute(STATUS).setDataType(ENUM).setNillable(false).setEnumOptions(Arrays.asList("RUNNING","FINISHED","FAILED")).setDescription("");
        addAttribute(MESSAGE).setDataType(TEXT).setNillable(true).setDescription("");
        addAttribute(PROGRESS).setDataType(INT).setNillable(false).setDescription("");
        addAttribute(IMPORTEDENTITIES).setDataType(TEXT).setNillable(true).setDescription("");
    }
}
