package org.molgenis.data.validation;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityType;

public interface EntityValidator
{
	void validate(Iterable<? extends Entity> entities, EntityType meta, DatabaseAction dbAction);
}
