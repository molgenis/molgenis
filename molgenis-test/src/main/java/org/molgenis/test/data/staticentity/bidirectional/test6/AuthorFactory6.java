package org.molgenis.test.data.staticentity.bidirectional.test6;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.test.data.staticentity.bidirectional.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorFactory6 extends AbstractSystemEntityFactory<Author, AuthorMetaData6, String>
{
	@Autowired
	AuthorFactory6(AuthorMetaData6 myEntityMeta)
	{
		super(Author.class, myEntityMeta);
	}
}
