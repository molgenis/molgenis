package org.molgenis.data.system;

import java.util.Date;

public class ImportRun {
    String id;
    Date startDate;
    Date endDate;
    String userName;
    Enum status;
    String message;
    int progress;
    String importedEntities;
}
