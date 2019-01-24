package org.molgenis.data.file.model;

import static org.molgenis.data.meta.AttributeType.HYPERLINK;
import static org.molgenis.data.meta.AttributeType.LONG;
import static org.molgenis.data.meta.AttributeType.STRING;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_ID;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LABEL;
import static org.molgenis.data.meta.model.EntityType.AttributeRole.ROLE_LOOKUP;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.data.system.model.RootSystemPackage.PACKAGE_SYSTEM;

import org.molgenis.data.meta.SystemEntityType;
import org.springframework.stereotype.Component;

@Component
public class FileMetaMetadata extends SystemEntityType {
  private static final String SIMPLE_NAME = "FileMeta";
  public static final String FILE_META = PACKAGE_SYSTEM + PACKAGE_SEPARATOR + SIMPLE_NAME;

  public static final String ID = "id";
  public static final String FILENAME = "filename";
  public static final String CONTENT_TYPE = "contentType";
  public static final String SIZE = "size";
  public static final String URL = "url";

  FileMetaMetadata() {
    super(SIMPLE_NAME, PACKAGE_SYSTEM);
  }

  @Override
  public void init() {
    setLabel("File metadata");
    addAttribute(ID, ROLE_ID).setVisible(false).setLabel("Id").setAuto(true);
    addAttribute(FILENAME, ROLE_LABEL, ROLE_LOOKUP)
        .setDataType(STRING)
        .setNillable(false)
        .setLabel("Filename");
    addAttribute(CONTENT_TYPE, ROLE_LOOKUP).setDataType(STRING).setLabel("Content-type");
    addAttribute(SIZE).setDataType(LONG).setLabel("Size").setDescription("File size in bytes");
    addAttribute(URL)
        .setDataType(HYPERLINK)
        .setLabel("URL")
        .setDescription("File download URL")
        .setUnique(true)
        .setNillable(false);

    setRowLevelSecured(true);
  }
}
