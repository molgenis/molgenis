package org.molgenis.test.data.staticentity.bidirectional.test1;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.test.data.staticentity.bidirectional.Author;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorFactory1 extends AbstractSystemEntityFactory<Author, AuthorMetaData1, String>
{
	@Autowired
	AuthorFactory1(AuthorMetaData1 myEntityMeta)
	{
		super(Author.class, myEntityMeta);
	}
}
