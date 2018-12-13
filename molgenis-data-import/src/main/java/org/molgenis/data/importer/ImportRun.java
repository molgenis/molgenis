package org.molgenis.data.importer;

import static org.molgenis.data.importer.ImportRunMetaData.ENDDATE;
import static org.molgenis.data.importer.ImportRunMetaData.ID;
import static org.molgenis.data.importer.ImportRunMetaData.IMPORTEDENTITIES;
import static org.molgenis.data.importer.ImportRunMetaData.MESSAGE;
import static org.molgenis.data.importer.ImportRunMetaData.NOTIFY;
import static org.molgenis.data.importer.ImportRunMetaData.PROGRESS;
import static org.molgenis.data.importer.ImportRunMetaData.STARTDATE;
import static org.molgenis.data.importer.ImportRunMetaData.STATUS;
import static org.molgenis.data.importer.ImportRunMetaData.USERNAME;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;
import org.molgenis.util.ValueLabel;

public class ImportRun extends StaticEntity {
  private static final List<ValueLabel> status_options;
  private static final List<ValueLabel> notify_options;

  static {
    status_options = new ArrayList<>();
    status_options.add(new org.molgenis.util.ValueLabel("RUNNING", "RUNNING"));
    status_options.add(new org.molgenis.util.ValueLabel("FINISHED", "FINISHED"));
    status_options.add(new org.molgenis.util.ValueLabel("FAILED", "FAILED"));

    notify_options = new ArrayList<>();
    notify_options.add(new org.molgenis.util.ValueLabel("API", "API"));
    notify_options.add(new org.molgenis.util.ValueLabel("UI", "UI"));
  }

  public ImportRun(Entity entity) {
    super(entity);
  }

  public ImportRun(EntityType entityType) {
    super(entityType);
  }

  public ImportRun(String id, EntityType entityType) {
    super(entityType);
    setId(id);
  }

  public String getId() {
    return getString(ID);
  }

  public void setId(String id) {
    set(ID, id);
  }

  public Instant getStartDate() {
    return getInstant(STARTDATE);
  }

  public void setStartDate(Instant startDate) {
    set(STARTDATE, startDate);
  }

  @CheckForNull
  public Instant getEndDate() {
    return getInstant(ENDDATE);
  }

  public void setEndDate(Instant endDate) {
    set(ENDDATE, endDate);
  }

  public String getUsername() {
    return getString(USERNAME);
  }

  public void setUsername(String username) {
    set(USERNAME, username);
  }

  public String getStatus() {
    return getString(STATUS);
  }

  public void setStatus(String status) {
    set(STATUS, status);
  }

  @CheckForNull
  public String getMessage() {
    return getString(MESSAGE);
  }

  public void setMessage(String message) {
    set(MESSAGE, message);
  }

  public int getProgress() {
    Integer progress = getInt(PROGRESS);
    return progress != null ? progress : 0;
  }

  public void setProgress(int progress) {
    set(PROGRESS, progress);
  }

  @CheckForNull
  public String getImportedEntities() {
    return getString(IMPORTEDENTITIES);
  }

  public void setImportedEntities(String importedEntities) {
    set(IMPORTEDENTITIES, importedEntities);
  }

  public boolean getNotify() {
    return getBoolean(NOTIFY) != null && getBoolean(NOTIFY);
  }

  public void setNotify(boolean notify) {
    set(NOTIFY, notify);
  }

  /** Status is enum. This method returns all available enum options. */
  public List<ValueLabel> getStatusOptions() {
    return status_options;
  }

  public List<ValueLabel> getNotifyOptions() {
    return notify_options;
  }
}
