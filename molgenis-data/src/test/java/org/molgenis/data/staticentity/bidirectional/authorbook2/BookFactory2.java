package org.molgenis.data.staticentity.bidirectional.authorbook2;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.staticentity.bidirectional.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookFactory2 extends AbstractSystemEntityFactory<Book, BookMetaData2, String>
{
	@Autowired
	BookFactory2(BookMetaData2 myRefEntityMeta, EntityPopulator entityPopulator)
	{
		super(Book.class, myRefEntityMeta, entityPopulator);
	}
}
