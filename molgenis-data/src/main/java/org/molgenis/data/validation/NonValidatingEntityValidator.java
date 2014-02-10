package org.molgenis.data.validation;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

public class NonValidatingEntityValidator implements EntityValidator
{

	@Override
	public void validate(Iterable<? extends Entity> entities, EntityMetaData meta) throws MolgenisValidationException
	{
		// Nothing
	}

}
