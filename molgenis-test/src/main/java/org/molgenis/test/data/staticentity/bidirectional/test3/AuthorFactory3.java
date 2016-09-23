package org.molgenis.test.data.staticentity.bidirectional.test3;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.test.data.staticentity.bidirectional.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorFactory3 extends AbstractSystemEntityFactory<Author, AuthorMetaData3, String>
{
	@Autowired
	AuthorFactory3(AuthorMetaData3 myEntityMeta)
	{
		super(Author.class, myEntityMeta);
	}
}
