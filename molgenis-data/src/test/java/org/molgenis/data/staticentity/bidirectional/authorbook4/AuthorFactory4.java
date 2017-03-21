package org.molgenis.data.staticentity.bidirectional.authorbook4;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.staticentity.bidirectional.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorFactory4 extends AbstractSystemEntityFactory<Author, AuthorMetaData4, String>
{
	@Autowired
	AuthorFactory4(AuthorMetaData4 myEntityMeta, EntityPopulator entityPopulator)
	{
		super(Author.class, myEntityMeta, entityPopulator);
	}
}
