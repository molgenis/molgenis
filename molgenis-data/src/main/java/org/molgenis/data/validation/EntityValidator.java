package org.molgenis.data.validation;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

public interface EntityValidator
{
	void validate(Iterable<? extends Entity> entities, EntityMetaData meta) throws MolgenisValidationException;
}
