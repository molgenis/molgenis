package org.molgenis.oneclickimporter.service;

import org.molgenis.data.meta.model.EntityType;
import org.molgenis.oneclickimporter.model.DataCollection;

public interface EntityService
{
	/**
	 * Create an {@link EntityType} from a {@link DataCollection}
	 *
	 * @param dataCollection
	 * @return the ID of the newly created {@link EntityType}
	 */
	EntityType createEntityType(DataCollection dataCollection);
}
