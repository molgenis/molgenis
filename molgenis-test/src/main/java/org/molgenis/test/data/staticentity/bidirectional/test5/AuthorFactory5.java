package org.molgenis.test.data.staticentity.bidirectional.test5;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.test.data.staticentity.bidirectional.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorFactory5 extends AbstractSystemEntityFactory<Author, AuthorMetaData5, String>
{
	@Autowired
	AuthorFactory5(AuthorMetaData5 myEntityMeta)
	{
		super(Author.class, myEntityMeta);
	}
}
