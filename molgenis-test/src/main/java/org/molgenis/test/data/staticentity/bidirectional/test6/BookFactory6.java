package org.molgenis.test.data.staticentity.bidirectional.test6;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.test.data.staticentity.bidirectional.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookFactory6 extends AbstractSystemEntityFactory<Book, BookMetaData6, String>
{
	@Autowired
	BookFactory6(BookMetaData6 myRefEntityMeta)
	{
		super(Book.class, myRefEntityMeta);
	}
}
