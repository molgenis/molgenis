package org.molgenis.test.data.staticentity.bidirectional;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//@Component
public class BookFactory extends AbstractSystemEntityFactory<Book, BookMetaData, String>
{
	@Autowired
	BookFactory(BookMetaData myRefEntityMeta)
	{
		super(Book.class, myRefEntityMeta);
	}
}
