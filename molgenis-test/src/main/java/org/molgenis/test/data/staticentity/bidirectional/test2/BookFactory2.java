package org.molgenis.test.data.staticentity.bidirectional.test2;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.test.data.staticentity.bidirectional.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookFactory2 extends AbstractSystemEntityFactory<Book, BookMetaData2, String>
{
	@Autowired
	BookFactory2(BookMetaData2 myRefEntityMeta)
	{
		super(Book.class, myRefEntityMeta);
	}
}
