package org.molgenis.framework.db;

import java.io.IOException;
import java.io.InputStream;

public interface EntitiesValidator
{
	public EntitiesValidationReport validate(InputStream is) throws IOException;

	@Deprecated
	public void setDatabase(Database db);
}
