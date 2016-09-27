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

import static com.google.common.collect.Lists.newArrayList;

/**
 * Generates entities to use in tests with a OneToMany relation in the form of Authors and Books, or Persons with self reference.
 */
@Component
public class OneToManyTestHarness
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

	public static final int ONE_TO_MANY_CASES = 6;

	public static final String BOOK_1 = "book1";
	public static final String BOOK_2 = "book2";
	public static final String BOOK_3 = "book3";

	public static final String AUTHOR_1 = "author1";
	public static final String AUTHOR_2 = "author2";
	public static final String AUTHOR_3 = "author3";

	public static final String ATTR_BOOKS = "books";
	public static final String ATTR_AUTHOR = "author";

	@PostConstruct
	public void postConstruct()
	{
	}

	/**
	 * Creates Author and Book entity test sets for a specific use case. Entities are always linked up as follows (when
	 * imported): author1 -> book1, author2 -> book2, author3 -> book3, and vice versa.
	 *
	 * Case 1: Author.books = nillable, Book.author = nillable | no ordering
	 * Case 2: Author.books = nillable, Book.author = required | no ordering
	 * Case 3: Author.books = required, Book.author = nillable | no ordering
	 * Case 4: Author.books = required, Book.author = required | no ordering
	 * Case 5: Author.books = nillable, Book.author = nillable | ascending order
	 * Case 6: Author.books = nillable, Book.author = nillable | descending order
	 */
	public AuthorsAndBooks createAuthorAndBookEntities(int testCase)
	{
		switch (testCase)
		{
			case 1:
				return createTestEntitiesPopulateAuthors(authorFactory1, bookFactory1, authorMetaData1,
						authorMetaData1);
			case 2:
				return createTestEntitiesPopulateBooks(authorFactory2, bookFactory2, authorMetaData2, bookMetaData2);
			case 3:
				return createTestEntitiesPopulateAuthors(authorFactory3, bookFactory3, authorMetaData3,
						authorMetaData3);
			case 4:
				return createTestEntitiesPopulateAuthors(authorFactory4, bookFactory4, authorMetaData4,
						authorMetaData4);
			case 5:
				return createTestEntitiesPopulateAuthors(authorFactory5, bookFactory5, authorMetaData5,
						authorMetaData5);
			case 6:
				return createTestEntitiesPopulateAuthors(authorFactory6, bookFactory6, authorMetaData6,
						authorMetaData6);
			default:
				throw new IllegalArgumentException("Unknown test case " + testCase);
		}
	}

	/**
	 * Create Author and Book test entities and set the Author.books fields.
	 */
	private AuthorsAndBooks createTestEntitiesPopulateAuthors(AbstractSystemEntityFactory authorFactory,
			AbstractSystemEntityFactory bookFactory, EntityMetaData authorMetaData, EntityMetaData bookMetaData)
	{
		List<Entity> authors = createAuthorEntities(authorFactory);
		List<Entity> books = createBookEntities(bookFactory);
		authors.get(0).set(ATTR_BOOKS, books.subList(0, 1)); // author1 -> book1
		authors.get(1).set(ATTR_BOOKS, books.subList(1, 2)); // author2 -> book2
		authors.get(2).set(ATTR_BOOKS, books.subList(2, 3)); // author3 -> book3
		return new AuthorsAndBooks(authors, books, authorMetaData, bookMetaData);
	}

	/**
	 * Create Author and Book test entities and set the Books.author fields.
	 */
	private AuthorsAndBooks createTestEntitiesPopulateBooks(AbstractSystemEntityFactory authorFactory,
			AbstractSystemEntityFactory bookFactory, EntityMetaData authorMetaData, EntityMetaData bookMetaData)
	{
		List<Entity> authors = createAuthorEntities(authorFactory);
		List<Entity> books = createBookEntities(bookFactory);
		books.get(0).set(ATTR_AUTHOR, authors.get(0)); // book1 -> author1
		books.get(1).set(ATTR_AUTHOR, authors.get(1)); // book2 -> author2
		books.get(2).set(ATTR_AUTHOR, authors.get(2)); // book3 -> author3
		return new AuthorsAndBooks(authors, books, authorMetaData, bookMetaData);
	}

	private List<Entity> createAuthorEntities(AbstractSystemEntityFactory authorFactory)
	{
		Entity author1 = authorFactory.create();
		author1.set(AuthorMetaData1.ID, AUTHOR_1);
		author1.set(AuthorMetaData1.LABEL, "Fabian");

		Entity author2 = authorFactory.create();
		author2.set(AuthorMetaData1.ID, AUTHOR_2);
		author2.set(AuthorMetaData1.LABEL, "Mechteld");

		Entity author3 = authorFactory.create();
		author3.set(AuthorMetaData1.ID, AUTHOR_3);
		author3.set(AuthorMetaData1.LABEL, "Henk");

		return newArrayList(author1, author2, author3);
	}

	private List<Entity> createBookEntities(AbstractSystemEntityFactory bookFactory)
	{
		Entity book1 = bookFactory.create();

		book1.set(BookMetaData1.ID, BOOK_1);
		book1.set(BookMetaData1.LABEL, "MOLGENIS for Dummies");

		Entity book2 = bookFactory.create();
		book2.set(BookMetaData1.ID, BOOK_2);
		book2.set(BookMetaData1.LABEL, "A history of MOLGENIS");

		Entity book3 = bookFactory.create();
		book3.set(BookMetaData1.ID, BOOK_3);
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
