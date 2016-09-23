package org.molgenis.test.data.staticentity.bidirectional.test4;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.test.data.staticentity.bidirectional.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorFactory4 extends AbstractSystemEntityFactory<Author, AuthorMetaData4, String>
{
	@Autowired
	AuthorFactory4(AuthorMetaData4 myEntityMeta)
	{
		super(Author.class, myEntityMeta);
	}
}
