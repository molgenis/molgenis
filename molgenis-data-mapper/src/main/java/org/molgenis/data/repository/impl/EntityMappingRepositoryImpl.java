package org.molgenis.data.repository.impl;

import org.molgenis.data.CrudRepository;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.meta.EntityMappingMetaData;
import org.molgenis.data.repository.EntityMappingRepository;

public class EntityMappingRepositoryImpl implements EntityMappingRepository
{
	public static final EntityMetaData META_DATA = new EntityMappingMetaData();

	private final CrudRepository repository;
	
	public EntityMappingRepositoryImpl(CrudRepository repository){
		this.repository = repository;
	}

	@Override
	public void addEntityMapping()
	{
		// TODO Auto-generated method stub
		
	}
	
}
