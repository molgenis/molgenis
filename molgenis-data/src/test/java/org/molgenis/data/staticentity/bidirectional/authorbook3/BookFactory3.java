package org.molgenis.data.staticentity.bidirectional.authorbook3;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.staticentity.bidirectional.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookFactory3 extends AbstractSystemEntityFactory<Book, BookMetaData3, String>
{
	@Autowired
	BookFactory3(BookMetaData3 myRefEntityMeta, EntityPopulator entityPopulator)
	{
		super(Book.class, myRefEntityMeta, entityPopulator);
	}
}
