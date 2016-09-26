package org.molgenis.test.data.staticentity.bidirectional.test2;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.test.data.staticentity.bidirectional.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorFactory2 extends AbstractSystemEntityFactory<Author, AuthorMetaData2, String>
{
	@Autowired
	AuthorFactory2(AuthorMetaData2 myEntityMeta)
	{
		super(Author.class, myEntityMeta);
	}
}
