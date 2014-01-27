package org.molgenis.data;

import org.molgenis.data.jpa.AbstractJpaRepository;
import org.molgenis.data.support.EntityMetaDataCache;

public class PersonRepository extends AbstractJpaRepository<Person>
{	
	public PersonRepository()
	{
		EntityMetaDataCache.add(new PersonMetaData());
	}
	
	@Override
	public PersonMetaData getEntityMetaData()
	{
		return (PersonMetaData)EntityMetaDataCache.get(Person.class.getSimpleName());
	}
}