package org.molgenis.data.file.model;

import static org.molgenis.data.file.model.FileMetaMetaData.CONTENT_TYPE;
import static org.molgenis.data.file.model.FileMetaMetaData.FILENAME;
import static org.molgenis.data.file.model.FileMetaMetaData.ID;
import static org.molgenis.data.file.model.FileMetaMetaData.SIZE;
import static org.molgenis.data.file.model.FileMetaMetaData.URL;

import javax.annotation.CheckForNull;
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

  @CheckForNull
  public String getContentType() {
    return getString(CONTENT_TYPE);
  }

  public void setSize(Long size) {
    set(SIZE, size);
  }

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
