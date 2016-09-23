package org.molgenis.test.data.staticentity.bidirectional.test1;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.test.data.staticentity.bidirectional.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookFactory1 extends AbstractSystemEntityFactory<Book, BookMetaData1, String>
{
	@Autowired
	BookFactory1(BookMetaData1 myRefEntityMeta)
	{
		super(Book.class, myRefEntityMeta);
	}
}
