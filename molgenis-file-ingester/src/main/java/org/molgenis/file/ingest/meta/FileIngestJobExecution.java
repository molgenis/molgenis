package org.molgenis.file.ingest.meta;

import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetadata.FILE;
import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetadata.FILE_INGEST_JOB_TYPE;
import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetadata.LOADER;
import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetadata.TARGET_ENTITY_ID;
import static org.molgenis.file.ingest.meta.FileIngestJobExecutionMetadata.URL;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.file.model.FileMeta;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.jobs.model.JobExecution;

public class FileIngestJobExecution extends JobExecution {
  public FileIngestJobExecution(Entity entity) {
    super(entity);
    setType(FILE_INGEST_JOB_TYPE);
  }

  public FileIngestJobExecution(EntityType entityType) {
    super(entityType);
    setType(FILE_INGEST_JOB_TYPE);
  }

  public FileIngestJobExecution(String identifier, EntityType entityType) {
    super(identifier, entityType);
    setType(FILE_INGEST_JOB_TYPE);
  }

  @Nullable
  @CheckForNull
  public FileMeta getFile() {
    return getEntity(FILE, FileMeta.class);
  }

  public void setFile(FileMeta value) {
    set(FILE, value);
  }

  public void setUrl(String url) {
    set(URL, url);
  }

  public String getUrl() {
    return getString(URL);
  }

  public String getTargetEntityId() {
    return getString(TARGET_ENTITY_ID);
  }

  public void setTargetEntityId(String targetEntityId) {
    set(TARGET_ENTITY_ID, targetEntityId);
  }

  public void setLoader(String loader) {
    set(LOADER, loader);
  }

  public String getLoader() {
    return getString(LOADER);
  }
}
