package org.molgenis.data.staticentity.bidirectional.authorbook1;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.staticentity.bidirectional.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookFactory1 extends AbstractSystemEntityFactory<Book, BookMetaData1, String>
{
	@Autowired
	BookFactory1(BookMetaData1 myRefEntityMeta, EntityPopulator entityPopulator)
	{
		super(Book.class, myRefEntityMeta, entityPopulator);
	}
}
