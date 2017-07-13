package org.molgenis.oneclickimporter.service;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.oneclickimporter.model.DataCollection;

public interface EntityService
{
	EntityType createEntity(DataCollection dataCollection);
}
