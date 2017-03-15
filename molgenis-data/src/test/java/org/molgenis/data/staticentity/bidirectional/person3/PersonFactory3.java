package org.molgenis.data.staticentity.bidirectional.person3;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.staticentity.bidirectional.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonFactory3 extends AbstractSystemEntityFactory<Person, PersonMetaData3, String>
{
	@Autowired
	PersonFactory3(PersonMetaData3 personMeta, EntityPopulator entityPopulator)
	{
		super(Person.class, personMeta, entityPopulator);
	}
}
