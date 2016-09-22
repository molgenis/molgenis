package org.molgenis.test.data.staticentity.bidirectional.test3;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.test.data.staticentity.bidirectional.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookFactory3 extends AbstractSystemEntityFactory<Book, BookMetaData3, String>
{
	@Autowired
	BookFactory3(BookMetaData3 myRefEntityMeta)
	{
		super(Book.class, myRefEntityMeta);
	}
}
