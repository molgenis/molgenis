package org.molgenis.test.data;

import org.molgenis.data.AbstractSystemEntityFactory;
import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.test.data.staticentity.bidirectional.person1.PersonFactory1;
import org.molgenis.test.data.staticentity.bidirectional.person1.PersonMetaData1;
import org.molgenis.test.data.staticentity.bidirectional.person2.PersonFactory2;
import org.molgenis.test.data.staticentity.bidirectional.person2.PersonMetaData2;
import org.molgenis.test.data.staticentity.bidirectional.person3.PersonFactory3;
import org.molgenis.test.data.staticentity.bidirectional.person3.PersonMetaData3;
import org.molgenis.test.data.staticentity.bidirectional.person4.PersonFactory4;
import org.molgenis.test.data.staticentity.bidirectional.person4.PersonMetaData4;
import org.molgenis.test.data.staticentity.bidirectional.authorbook1.AuthorFactory1;
import org.molgenis.test.data.staticentity.bidirectional.authorbook1.AuthorMetaData1;
import org.molgenis.test.data.staticentity.bidirectional.authorbook1.BookFactory1;
import org.molgenis.test.data.staticentity.bidirectional.authorbook1.BookMetaData1;
import org.molgenis.test.data.staticentity.bidirectional.authorbook2.AuthorFactory2;
import org.molgenis.test.data.staticentity.bidirectional.authorbook2.AuthorMetaData2;
import org.molgenis.test.data.staticentity.bidirectional.authorbook2.BookFactory2;
import org.molgenis.test.data.staticentity.bidirectional.authorbook2.BookMetaData2;
import org.molgenis.test.data.staticentity.bidirectional.authorbook3.AuthorFactory3;
import org.molgenis.test.data.staticentity.bidirectional.authorbook3.AuthorMetaData3;
import org.molgenis.test.data.staticentity.bidirectional.authorbook3.BookFactory3;
import org.molgenis.test.data.staticentity.bidirectional.authorbook3.BookMetaData3;
import org.molgenis.test.data.staticentity.bidirectional.authorbook4.AuthorFactory4;
import org.molgenis.test.data.staticentity.bidirectional.authorbook4.AuthorMetaData4;
import org.molgenis.test.data.staticentity.bidirectional.authorbook4.BookFactory4;
import org.molgenis.test.data.staticentity.bidirectional.authorbook4.BookMetaData4;
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
	AuthorMetaData1 authorMetaData1;
	@Autowired
	AuthorMetaData2 authorMetaData2;
	@Autowired
	AuthorMetaData3 authorMetaData3;
	@Autowired
	AuthorMetaData4 authorMetaData4;
	@Autowired
	BookFactory1 bookFactory1;
	@Autowired
	BookFactory2 bookFactory2;
	@Autowired
	BookFactory3 bookFactory3;
	@Autowired
	BookFactory4 bookFactory4;
	@Autowired
	BookMetaData1 bookMetaData1;
	@Autowired
	BookMetaData2 bookMetaData2;
	@Autowired
	BookMetaData3 bookMetaData3;
	@Autowired
	BookMetaData4 bookMetaData4;

	@Autowired
	PersonFactory1 personFactory1;
	@Autowired
	PersonFactory2 personFactory2;
	@Autowired
	PersonFactory3 personFactory3;
	@Autowired
	PersonFactory4 personFactory4;
	@Autowired
	PersonMetaData1 personMetaData1;
	@Autowired
	PersonMetaData2 personMetaData2;
	@Autowired
	PersonMetaData3 personMetaData3;
	@Autowired
	PersonMetaData4 personMetaData4;

	public static final int ONE_TO_MANY_CASES = 4;

	public static final String BOOK_1 = "book1";
	public static final String BOOK_2 = "book2";
	public static final String BOOK_3 = "book3";

	public static final String AUTHOR_1 = "author1";
	public static final String AUTHOR_2 = "author2";
	public static final String AUTHOR_3 = "author3";

	public static final String ATTR_BOOKS = "books";
	public static final String ATTR_AUTHOR = "author";

	public static final String PERSON_1 = "person1";
	public static final String PERSON_2 = "person2";
	public static final String PERSON_3 = "person3";

	public static final String ATTR_PARENT = "parent";
	public static final String ATTR_CHILDREN = "children";

	@PostConstruct
	public void postConstruct()
	{
	}

	/**
	 * Creates Author and Book entity test sets for a specific use case. Entities are always linked as follows (when
	 * imported): author1 -> book1, author2 -> book2, author3 -> book3, and vice versa.
	 * <p>
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
				return createTestEntitiesSetAuthorField(authorFactory1, bookFactory1, authorMetaData1, bookMetaData1);
			case 2:
				return createTestEntitiesSetBooksField(authorFactory2, bookFactory2, authorMetaData2, bookMetaData2);
			case 3:
				return createTestEntitiesSetAuthorField(authorFactory3, bookFactory3, authorMetaData3, bookMetaData3);
			case 4:
				return createTestEntitiesSetAuthorField(authorFactory4, bookFactory4, authorMetaData4, bookMetaData4);
			default:
				throw new IllegalArgumentException("Unknown test case " + testCase);
		}
	}

	/**
	 * Creates Person entity test sets for a specific use case. Entities are always linked as folows (when imported):
	 * person1 -> person2, person2 -> person1, person3 -> person2
	 * <p>
	 * Case 1: Person.parent = nillable, Person.children = nillable
	 * Case 2: Person.parent = nillable, Person.children = required
	 * Case 3: Person.parent = required, Person.children = nillable
	 * Case 4: Person.parent = required, Person.children = required
	 */
	public List<Entity> createPersonEntities(int testCase)
	{
		switch (testCase)
		{
			case 1:
				return createPersonEntities(personFactory1);
			case 2:
				return createPersonEntities(personFactory2);
			case 3:
				return createPersonEntities(personFactory3);
			case 4:
				return createPersonEntities(personFactory4);
			default:
				throw new IllegalArgumentException("Unknown test case " + testCase);
		}
	}

	/**
	 * Create Author and Book test entities and set the Author.books fields.
	 */
	private AuthorsAndBooks createTestEntitiesSetAuthorField(AbstractSystemEntityFactory authorFactory,
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
	private AuthorsAndBooks createTestEntitiesSetBooksField(AbstractSystemEntityFactory authorFactory,
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
	 * Create Person entities and set the Person.children field.
	 */
	private List<Entity> createPersonEntities(AbstractSystemEntityFactory personFactory)
	{
		Entity person1 = personFactory.create();
		person1.set(PersonMetaData1.ID, PERSON_1);
		person1.set(PersonMetaData1.LABEL, "Roderique");

		Entity person2 = personFactory.create();
		person2.set(PersonMetaData1.ID, PERSON_2);
		person2.set(PersonMetaData1.LABEL, "Sjonny");

		Entity person3 = personFactory.create();
		person3.set(PersonMetaData1.ID, PERSON_3);
		person3.set(PersonMetaData1.LABEL, "Klaas");

		person1.set(PersonMetaData1.ATTR_CHILDREN, newArrayList(person2));
		person2.set(PersonMetaData1.ATTR_CHILDREN, newArrayList(person3));
		person3.set(PersonMetaData1.ATTR_CHILDREN, newArrayList(person1));

		return newArrayList(person1, person2, person3);
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
