package org.molgenis.metadata.manager.service;

import java.util.List;
import org.molgenis.metadata.manager.model.EditorAttributeResponse;
import org.molgenis.metadata.manager.model.EditorEntityType;
import org.molgenis.metadata.manager.model.EditorEntityTypeResponse;
import org.molgenis.metadata.manager.model.EditorPackageIdentifier;

public interface MetadataManagerService {
  List<EditorPackageIdentifier> getEditorPackages();

  EditorEntityTypeResponse getEditorEntityType(String entityTypeId);

  EditorEntityTypeResponse createEditorEntityType();

  void upsertEntityType(EditorEntityType editorEntityType);

  EditorAttributeResponse createEditorAttribute();
}
