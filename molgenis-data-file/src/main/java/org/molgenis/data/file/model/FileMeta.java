package org.molgenis.data.file.model;

import static org.molgenis.data.file.model.FileMetaMetadata.CONTENT_TYPE;
import static org.molgenis.data.file.model.FileMetaMetadata.FILENAME;
import static org.molgenis.data.file.model.FileMetaMetadata.ID;
import static org.molgenis.data.file.model.FileMetaMetadata.SIZE;
import static org.molgenis.data.file.model.FileMetaMetadata.URL;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;
import org.molgenis.data.support.StaticEntity;

public class FileMeta extends StaticEntity {
  public FileMeta(Entity entity) {
    super(entity);
  }

  public FileMeta(EntityType entityType) {
    super(entityType);
  }

  public FileMeta(String id, EntityType entityType) {
    setId(id);
  }

  public void setId(String id) {
    set(ID, id);
  }

  public String getId() {
    return getString(ID);
  }

  public void setFilename(String filename) {
    set(FILENAME, filename);
  }

  public String getFilename() {
    return getString(FILENAME);
  }

  public void setContentType(String contentType) {
    set(CONTENT_TYPE, contentType);
  }

  @Nullable
  @CheckForNull
  public String getContentType() {
    return getString(CONTENT_TYPE);
  }

  public void setSize(Long size) {
    set(SIZE, size);
  }

  @Nullable
  @CheckForNull
  public Long getSize() {
    return getLong(SIZE);
  }

  public void setUrl(String url) {
    set(URL, url);
  }

  public String getUrl() {
    return getString(URL);
  }
}
