package org.molgenis.data.system;

import java.util.Date;

import com.google.auto.value.AutoValue;

@AutoValue
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
