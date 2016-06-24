package org.molgenis.data.validation;

import org.molgenis.data.DatabaseAction;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

public interface EntityValidator
{
	void validate(Iterable<? extends Entity> entities, EntityMetaData meta, DatabaseAction dbAction)
			throws MolgenisValidationException;
}
