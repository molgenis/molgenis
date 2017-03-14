package org.molgenis.data.staticentity.bidirectional.authorbook3;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.staticentity.bidirectional.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorFactory3 extends AbstractSystemEntityFactory<Author, AuthorMetaData3, String>
{
	@Autowired
	AuthorFactory3(AuthorMetaData3 myEntityMeta, EntityPopulator entityPopulator)
	{
		super(Author.class, myEntityMeta, entityPopulator);
	}
}
