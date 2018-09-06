package org.molgenis.data.importer;

public enum MetadataAction {
  /** Create new metadata */
  ADD,

  /** Update existing metadata */
  UPDATE,

  /** Create new metadata or update existing metadata */
  UPSERT,

  /** Ignore metadata */
  IGNORE
}
