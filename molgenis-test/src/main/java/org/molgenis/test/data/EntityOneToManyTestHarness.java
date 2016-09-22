package org.molgenis.test.data;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.test.data.staticentity.bidirectional.test1.AuthorFactory1;
import org.molgenis.test.data.staticentity.bidirectional.test1.AuthorMetaData1;
import org.molgenis.test.data.staticentity.bidirectional.test1.BookFactory1;
import org.molgenis.test.data.staticentity.bidirectional.test1.BookMetaData1;
import org.molgenis.test.data.staticentity.bidirectional.test2.AuthorFactory2;
import org.molgenis.test.data.staticentity.bidirectional.test2.AuthorMetaData2;
import org.molgenis.test.data.staticentity.bidirectional.test2.BookFactory2;
import org.molgenis.test.data.staticentity.bidirectional.test2.BookMetaData2;
import org.molgenis.test.data.staticentity.bidirectional.test3.AuthorFactory3;
import org.molgenis.test.data.staticentity.bidirectional.test3.AuthorMetaData3;
import org.molgenis.test.data.staticentity.bidirectional.test3.BookFactory3;
import org.molgenis.test.data.staticentity.bidirectional.test3.BookMetaData3;
import org.molgenis.test.data.staticentity.bidirectional.test4.AuthorFactory4;
import org.molgenis.test.data.staticentity.bidirectional.test4.AuthorMetaData4;
import org.molgenis.test.data.staticentity.bidirectional.test4.BookFactory4;
import org.molgenis.test.data.staticentity.bidirectional.test4.BookMetaData4;
import org.molgenis.test.data.staticentity.bidirectional.test5.AuthorFactory5;
import org.molgenis.test.data.staticentity.bidirectional.test5.AuthorMetaData5;
import org.molgenis.test.data.staticentity.bidirectional.test5.BookFactory5;
import org.molgenis.test.data.staticentity.bidirectional.test5.BookMetaData5;
import org.molgenis.test.data.staticentity.bidirectional.test6.AuthorFactory6;
import org.molgenis.test.data.staticentity.bidirectional.test6.AuthorMetaData6;
import org.molgenis.test.data.staticentity.bidirectional.test6.BookFactory6;
import org.molgenis.test.data.staticentity.bidirectional.test6.BookMetaData6;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

import static autovalue.shaded.com.google.common.common.collect.Lists.newArrayList;

@Component
public class EntityOneToManyTestHarness
{
	@Autowired
	AuthorFactory1 authorFactory1;
	@Autowired
	AuthorFactory2 authorFactory2;
	@Autowired
	AuthorFactory3 authorFactory3;
	@Autowired
	AuthorFactory4 authorFactory4;
	@Autowired
	AuthorFactory5 authorFactory5;
	@Autowired
	AuthorFactory6 authorFactory6;
	@Autowired
	AuthorMetaData1 authorMetaData1;
	@Autowired
	AuthorMetaData2 authorMetaData2;
	@Autowired
	AuthorMetaData3 authorMetaData3;
	@Autowired
	AuthorMetaData4 authorMetaData4;
	@Autowired
	AuthorMetaData5 authorMetaData5;
	@Autowired
	AuthorMetaData6 authorMetaData6;
	@Autowired
	BookFactory1 bookFactory1;
	@Autowired
	BookFactory2 bookFactory2;
	@Autowired
	BookFactory3 bookFactory3;
	@Autowired
	BookFactory4 bookFactory4;
	@Autowired
	BookFactory5 bookFactory5;
	@Autowired
	BookFactory6 bookFactory6;
	@Autowired
	BookMetaData1 bookMetaData1;
	@Autowired
	BookMetaData2 bookMetaData2;
	@Autowired
	BookMetaData3 bookMetaData3;
	@Autowired
	BookMetaData4 bookMetaData4;
	@Autowired
	BookMetaData5 bookMetaData5;
	@Autowired
	BookMetaData6 bookMetaData6;

	@PostConstruct
	public void postConstruct()
	{
	}

	/**
	 * Test case 1: author.books nillable, book.author nillable, no ordering
	 */
	public AuthorsAndBooks createEntities1()
	{
		List<Entity> books = createBookEntities(bookFactory1);
		List<Entity> authors = createAuthorEntities(authorFactory1);
		authors.get(0).set(AuthorMetaData1.ATTR_BOOKS, books.subList(0, 1)); // author1 -> book1
		authors.get(1).set(AuthorMetaData1.ATTR_BOOKS, books.subList(1, 2)); // author2 -> book2
		authors.get(2).set(AuthorMetaData1.ATTR_BOOKS, books.subList(2, 3)); // author3 -> book3

		return new AuthorsAndBooks(authors, books, authorMetaData1, bookMetaData1);
	}

	/**
	 * Test case 2: author.books nillable, book.author required, no ordering
	 */
	public AuthorsAndBooks createEntities2()
	{
		List<Entity> books = createBookEntities(bookFactory2);
		List<Entity> authors = createAuthorEntities(authorFactory2);
		authors.get(0).set(AuthorMetaData1.ATTR_BOOKS, books.subList(0, 1)); // author1 -> book1
		authors.get(1).set(AuthorMetaData1.ATTR_BOOKS, books.subList(1, 2)); // author2 -> book2
		authors.get(2).set(AuthorMetaData1.ATTR_BOOKS, books.subList(2, 3)); // author3 -> book3
		return new AuthorsAndBooks(authors, books, authorMetaData2, bookMetaData2);
	}

	/**
	 * Test case 3: author.books required, book.author nillable, no ordering
	 */
	public AuthorsAndBooks createEntities3()
	{
		List<Entity> books = createBookEntities(bookFactory3);
		List<Entity> authors = createAuthorEntities(authorFactory3);
		authors.get(0).set(AuthorMetaData1.ATTR_BOOKS, books.subList(0, 1)); // author1 -> book1
		authors.get(1).set(AuthorMetaData1.ATTR_BOOKS, books.subList(1, 2)); // author2 -> book2
		authors.get(2).set(AuthorMetaData1.ATTR_BOOKS, books.subList(2, 3)); // author3 -> book3
		return new AuthorsAndBooks(authors, books, authorMetaData3, bookMetaData3);
	}

	/**
	 * Test case 4: author.books required, book.author required, no ordering
	 */
	public AuthorsAndBooks createEntities4()
	{
		List<Entity> books = createBookEntities(bookFactory4);
		List<Entity> authors = createAuthorEntities(authorFactory4);
		authors.get(0).set(AuthorMetaData1.ATTR_BOOKS, books.subList(0, 1)); // author1 -> book1
		authors.get(1).set(AuthorMetaData1.ATTR_BOOKS, books.subList(1, 2)); // author2 -> book2
		authors.get(2).set(AuthorMetaData1.ATTR_BOOKS, books.subList(2, 3)); // author3 -> book3
		return new AuthorsAndBooks(authors, books, authorMetaData4, bookMetaData4);
	}

	/**
	 * Test case 5: author.books nillable, book.author nillable, ascending order
	 */
	public AuthorsAndBooks createEntities5()
	{
		List<Entity> books = createBookEntities(bookFactory5);
		List<Entity> authors = createAuthorEntities(authorFactory5);
		authors.get(0).set(AuthorMetaData1.ATTR_BOOKS, books.subList(0, 1)); // author1 -> book1
		authors.get(1).set(AuthorMetaData1.ATTR_BOOKS, books.subList(1, 2)); // author2 -> book2
		authors.get(2).set(AuthorMetaData1.ATTR_BOOKS, books.subList(2, 3)); // author3 -> book3
		return new AuthorsAndBooks(authors, books, authorMetaData5, bookMetaData5);
	}

	/**
	 * Test case 6: author.books nillable, book.author nillable, descending order
	 */
	public AuthorsAndBooks createEntities6()
	{
		List<Entity> books = createBookEntities(bookFactory6);
		List<Entity> authors = createAuthorEntities(authorFactory6);
		authors.get(0).set(AuthorMetaData1.ATTR_BOOKS, books.subList(0, 1)); // author1 -> book1
		authors.get(1).set(AuthorMetaData1.ATTR_BOOKS, books.subList(1, 2)); // author2 -> book2
		authors.get(2).set(AuthorMetaData1.ATTR_BOOKS, books.subList(2, 3)); // author3 -> book3
		return new AuthorsAndBooks(authors, books, authorMetaData6, bookMetaData6);
	}

	private List<Entity> createAuthorEntities(AbstractSystemEntityFactory authorFactory)
	{
		Entity author1 = authorFactory.create();
		author1.set(AuthorMetaData1.ID, "author1");
		author1.set(AuthorMetaData1.LABEL, "Fabian");

		Entity author2 = authorFactory.create();
		author2.set(AuthorMetaData1.ID, "author2");
		author2.set(AuthorMetaData1.LABEL, "Mechteld");

		Entity author3 = authorFactory.create();
		author3.set(AuthorMetaData1.ID, "author3");
		author3.set(AuthorMetaData1.LABEL, "Henk");

		return newArrayList(author1, author2, author3);
	}

	private List<Entity> createBookEntities(AbstractSystemEntityFactory bookFactory)
	{
		Entity book1 = bookFactory.create();

		book1.set(BookMetaData1.ID, "book1");
		book1.set(BookMetaData1.LABEL, "MOLGENIS for Dummies");

		Entity book2 = bookFactory.create();
		book2.set(BookMetaData1.ID, "book2");
		book2.set(BookMetaData1.LABEL, "A history of MOLGENIS");

		Entity book3 = bookFactory.create();
		book3.set(BookMetaData1.ID, "book3");
		book3.set(BookMetaData1.LABEL, "Do you know where MOLGENIS?");

		return newArrayList(book1, book2, book3);
	}

	/**
	 * Simple container for Author and Book entities so they can be requested as one.
	 */
	public class AuthorsAndBooks
	{
		List<Entity> books;
		List<Entity> authors;
		EntityMetaData bookMetaData;
		EntityMetaData authorMetaData;

		AuthorsAndBooks(List<Entity> authors, List<Entity> books, EntityMetaData authorMetaData,
				EntityMetaData bookMetaData)
		{
			this.authors = authors;
			this.books = books;
			this.authorMetaData = authorMetaData;
			this.bookMetaData = bookMetaData;
		}

		public List<Entity> getBooks()
		{
			return books;
		}

		public List<Entity> getAuthors()
		{
			return authors;
		}

		public EntityMetaData getBookMetaData()
		{
			return bookMetaData;
		}

		public EntityMetaData getAuthorMetaData()
		{
			return authorMetaData;
		}
	}
}
