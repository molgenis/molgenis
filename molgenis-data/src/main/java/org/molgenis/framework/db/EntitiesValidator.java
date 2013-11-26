package org.molgenis.framework.db;

import java.io.File;
import java.io.IOException;

public interface EntitiesValidator
{
	public EntitiesValidationReport validate(File file) throws IOException;
}
