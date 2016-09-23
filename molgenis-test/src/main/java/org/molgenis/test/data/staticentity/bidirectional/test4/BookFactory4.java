package org.molgenis.test.data.staticentity.bidirectional.test4;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.test.data.staticentity.bidirectional.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookFactory4 extends AbstractSystemEntityFactory<Book, BookMetaData4, String>
{
	@Autowired
	BookFactory4(BookMetaData4 myRefEntityMeta)
	{
		super(Book.class, myRefEntityMeta);
	}
}
