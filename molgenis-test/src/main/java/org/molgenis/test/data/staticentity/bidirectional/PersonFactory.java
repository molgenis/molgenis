package org.molgenis.test.data.staticentity.bidirectional;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PersonFactory extends AbstractSystemEntityFactory<Person, PersonMetaData, String>
{
	@Autowired
	PersonFactory(PersonMetaData personMeta)
	{
		super(Person.class, personMeta);
	}
}
