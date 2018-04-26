package org.molgenis.data.staticentity.bidirectional.authorbook1;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.staticentity.bidirectional.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorFactory1 extends AbstractSystemEntityFactory<Author, AuthorMetaData1, String>
{
	@Autowired
	AuthorFactory1(AuthorMetaData1 myEntityMeta, EntityPopulator entityPopulator)
	{
		super(Author.class, myEntityMeta, entityPopulator);
	}
}
