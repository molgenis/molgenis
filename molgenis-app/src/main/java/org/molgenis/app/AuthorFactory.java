package org.molgenis.app;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AuthorFactory extends AbstractSystemEntityFactory<Author, AuthorMetaData, String>
{
	@Autowired
	AuthorFactory(AuthorMetaData myEntityMeta)
	{
		super(Author.class, myEntityMeta);
	}
}
