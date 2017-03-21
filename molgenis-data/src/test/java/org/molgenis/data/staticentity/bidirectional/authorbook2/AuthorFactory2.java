package org.molgenis.data.staticentity.bidirectional.authorbook2;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.staticentity.bidirectional.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorFactory2 extends AbstractSystemEntityFactory<Author, AuthorMetaData2, String>
{
	@Autowired
	AuthorFactory2(AuthorMetaData2 myEntityMeta, EntityPopulator entityPopulator)
	{
		super(Author.class, myEntityMeta, entityPopulator);
	}
}
