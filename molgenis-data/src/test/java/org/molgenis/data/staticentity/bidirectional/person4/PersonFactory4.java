package org.molgenis.data.staticentity.bidirectional.person4;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.staticentity.bidirectional.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonFactory4 extends AbstractSystemEntityFactory<Person, PersonMetaData4, String>
{
	@Autowired
	PersonFactory4(PersonMetaData4 personMeta, EntityPopulator entityPopulator)
	{
		super(Person.class, personMeta, entityPopulator);
	}
}
