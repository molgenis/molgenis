package org.molgenis.test.data.staticentity.bidirectional.test5;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.test.data.staticentity.bidirectional.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BookFactory5 extends AbstractSystemEntityFactory<Book, BookMetaData5, String>
{
	@Autowired
	BookFactory5(BookMetaData5 myRefEntityMeta)
	{
		super(Book.class, myRefEntityMeta);
	}
}
