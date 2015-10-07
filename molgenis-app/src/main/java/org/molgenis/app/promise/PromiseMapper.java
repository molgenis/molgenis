package org.molgenis.app.promise;

import java.io.IOException;

public interface PromiseMapper
{
	public String getId();

	public MappingReport map(String projectName) throws IOException;
}
