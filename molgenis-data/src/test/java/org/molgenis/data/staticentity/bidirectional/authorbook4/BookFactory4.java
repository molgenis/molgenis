package org.molgenis.data.staticentity.bidirectional.authorbook4;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.populate.EntityPopulator;
import org.molgenis.data.staticentity.bidirectional.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookFactory4 extends AbstractSystemEntityFactory<Book, BookMetaData4, String>
{
	@Autowired
	BookFactory4(BookMetaData4 myRefEntityMeta, EntityPopulator entityPopulator)
	{
		super(Book.class, myRefEntityMeta, entityPopulator);
	}
}
