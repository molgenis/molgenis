package org.molgenis.data.repository.impl;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.AttributeMappingMetaData;
import org.molgenis.data.repository.AttributeMappingRepository;

public class AttributeMappingRepositoryImpl implements AttributeMappingRepository
{
	public static final EntityMetaData META_DATA = new AttributeMappingMetaData();
	private CrudRepository repository;
	
	public AttributeMappingRepositoryImpl(CrudRepository attributeMappingCrudRepository)
	{
		this.repository = repository;
	}

}
