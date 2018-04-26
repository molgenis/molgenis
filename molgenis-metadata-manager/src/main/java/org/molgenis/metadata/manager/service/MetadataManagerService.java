package org.molgenis.metadata.manager.service;

import org.molgenis.metadata.manager.model.EditorAttributeResponse;
import org.molgenis.metadata.manager.model.EditorEntityType;
import org.molgenis.metadata.manager.model.EditorEntityTypeResponse;
import org.molgenis.metadata.manager.model.EditorPackageIdentifier;

import java.util.List;

public interface MetadataManagerService
{
	List<EditorPackageIdentifier> getEditorPackages();

	EditorEntityTypeResponse getEditorEntityType(String entityTypeId);

	EditorEntityTypeResponse createEditorEntityType();

	void upsertEntityType(EditorEntityType editorEntityType);

	EditorAttributeResponse createEditorAttribute();
}
