package org.molgenis.app.promise;

public interface PromiseMapper
{
	public String getId();

	public MappingReport map(String projectName);
}
