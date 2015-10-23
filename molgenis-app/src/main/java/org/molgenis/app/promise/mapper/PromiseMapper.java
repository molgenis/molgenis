package org.molgenis.app.promise.mapper;

import org.molgenis.data.Entity;

public interface PromiseMapper
{
	public String getId();

	public MappingReport map(Entity project);
}
