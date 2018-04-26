package org.molgenis.data.staticentity.bidirectional.person2;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.staticentity.bidirectional.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonFactory2 extends AbstractSystemEntityFactory<Person, PersonMetaData2, String>
{
	@Autowired
	PersonFactory2(PersonMetaData2 personMeta, EntityPopulator entityPopulator)
	{
		super(Person.class, personMeta, entityPopulator);
	}
}
