package org.molgenis.integrationtest.platform;

import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.elasticsearch.index.job.IndexService;
import org.molgenis.test.data.OneToManyTestHarness;
import org.molgenis.test.data.staticentity.bidirectional.authorbook1.AuthorMetaData1;
import org.molgenis.test.data.staticentity.bidirectional.authorbook1.BookMetaData1;
import org.molgenis.test.data.staticentity.bidirectional.person1.PersonMetaData1;
import org.molgenis.test.data.staticentity.bidirectional.person2.PersonMetaData2;
import org.molgenis.test.data.staticentity.bidirectional.person3.PersonMetaData3;
import org.molgenis.test.data.staticentity.bidirectional.person4.PersonMetaData4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.transaction.annotation.Transactional;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.newHashSet;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.integrationtest.platform.PlatformIT.*;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.molgenis.test.data.OneToManyTestHarness.*;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class OneToManyIT extends AbstractTestNGSpringContextTests
{
	private final Logger LOG = LoggerFactory.getLogger(OneToManyIT.class);

	@Autowired
	private IndexService indexService;
	@Autowired
	private OneToManyTestHarness oneToManyTestHarness;
	@Autowired
	private DataService dataService;


	@BeforeClass
	public void setUp()
	{
		List<GrantedAuthority> authorities = newArrayList();
		for (int i = 1; i <= ONE_TO_MANY_CASES; i++)
		{
			authorities.addAll(makeAuthorities("sys_Author" + i, true, true, true));
			authorities.addAll(makeAuthorities("sys_Book" + i, true, true, true));
			authorities.addAll(makeAuthorities("sys_Person" + i, true, true, true));
		}

		SecurityContextHolder.getContext()
				.setAuthentication(new TestingAuthenticationToken("user", "user", authorities));
		waitForWorkToBeFinished(indexService, LOG);
	}

	@AfterMethod
	public void afterMethod()
	{
		runAsSystem(() ->
		{
			deleteAuthorsThenBooks(1);
			deleteBooksThenAuthors(2);
			deleteAuthorsThenBooks(3);
			deleteAuthorsThenBooks(4);
			dataService.deleteAll(PersonMetaData1.NAME);
			dataService.deleteAll(PersonMetaData2.NAME);
			dataService.deleteAll(PersonMetaData3.NAME);
			dataService.deleteAll(PersonMetaData4.NAME);
		});
		waitForWorkToBeFinished(indexService, LOG);
	}

	@Test(singleThreaded = true, dataProvider = "oneToManyTestCaseDataProvider")
	public void testOneToManyAuthorAndBookInsert(int testCase)
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(testCase);

		String bookEntityName = authorsAndBooks.getBookMetaData().getName();
		assertEquals(dataService.findOneById(bookEntityName, BOOK_1).getEntity(ATTR_AUTHOR).getIdValue(), AUTHOR_1);
		assertEquals(dataService.findOneById(bookEntityName, BOOK_2).getEntity(ATTR_AUTHOR).getIdValue(), AUTHOR_2);
		assertEquals(dataService.findOneById(bookEntityName, BOOK_3).getEntity(ATTR_AUTHOR).getIdValue(), AUTHOR_3);

		String authorEntityName = authorsAndBooks.getAuthorMetaData().getName();
		assertEquals(dataService.findOneById(authorEntityName, AUTHOR_1).getEntities(ATTR_BOOKS).iterator().next()
				.getIdValue(), BOOK_1);
		assertEquals(dataService.findOneById(authorEntityName, AUTHOR_2).getEntities(ATTR_BOOKS).iterator().next()
				.getIdValue(), BOOK_2);
		assertEquals(dataService.findOneById(authorEntityName, AUTHOR_3).getEntities(ATTR_BOOKS).iterator().next()
				.getIdValue(), BOOK_3);
	}

	@Test(singleThreaded = true, dataProvider = "oneToManyTestCaseDataProvider")
	public void testOneToManyPersonInsert(int testCase)
	{
		List<Entity> persons = importPersons(testCase);

		String personEntityName = persons.get(0).getEntityMetaData().getName();
		Entity person1 = dataService.findOneById(personEntityName, PERSON_1);
		Entity person2 = dataService.findOneById(personEntityName, PERSON_2);
		Entity person3 = dataService.findOneById(personEntityName, PERSON_3);

		assertEquals(person1.getEntity(ATTR_PARENT).getIdValue(), PERSON_3);
		assertEquals(person2.getEntity(ATTR_PARENT).getIdValue(), PERSON_1);
		assertEquals(person3.getEntity(ATTR_PARENT).getIdValue(), PERSON_2);

		assertEquals(
				StreamSupport.stream(person1.getEntities(ATTR_CHILDREN).spliterator(), false).map(Entity::getIdValue)
						.collect(toSet()), newHashSet(PERSON_2));
		assertEquals(
				StreamSupport.stream(person2.getEntities(ATTR_CHILDREN).spliterator(), false).map(Entity::getIdValue)
						.collect(toSet()), newHashSet(PERSON_3));
		assertEquals(
				StreamSupport.stream(person3.getEntities(ATTR_CHILDREN).spliterator(), false).map(Entity::getIdValue)
						.collect(toSet()), newHashSet(PERSON_1));
	}

	@DataProvider(name = "oneToManyTestCaseDataProvider")
	public Object[][] oneToManyTestCaseDataProvider()
	{
		return new Object[][] { { 1 }, { 2 }, { 3 }, { 4 } };
	}

	@Test(singleThreaded = true)
	@Transactional
	public void testL1OneToManySingleEntityUpdate()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(1);
		try
		{
			Entity book1 = dataService.findOneById(authorsAndBooks.getBookMetaData().getName(), BOOK_1);
			Entity author1 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getName(), AUTHOR_1);
			Entity author2 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getName(), AUTHOR_2);

			book1.set(BookMetaData1.AUTHOR, author2);
			dataService.update(book1.getEntityMetaData().getName(), book1);

			Entity author1RetrievedAgain = dataService
					.findOneById(authorsAndBooks.getAuthorMetaData().getName(), author1.getIdValue());
			assertEquals(Collections.emptyList(),
					Lists.newArrayList(author1RetrievedAgain.getEntities(AuthorMetaData1.ATTR_BOOKS)));

			Entity author2Retrieved = dataService
					.findOneById(authorsAndBooks.getAuthorMetaData().getName(), author2.getIdValue());
			Iterable<Entity> author2Books = author2Retrieved.getEntities(AuthorMetaData1.ATTR_BOOKS);

			// expected behavior: book.author changed, new author.books order is undefined
			Set<Object> retrievedAuthor2BookIds = StreamSupport.stream(author2Books.spliterator(), false)
					.map(Entity::getIdValue).collect(toSet());
			assertEquals(retrievedAuthor2BookIds, newHashSet(BOOK_2, BOOK_1));
		}
		finally
		{
			dataService.deleteAll(authorsAndBooks.getBookMetaData().getName());
			dataService.deleteAll(authorsAndBooks.getAuthorMetaData().getName());
		}
	}

	@Test(singleThreaded = true)
	@Transactional
	public void testL1OneToManyStreamingEntityUpdate()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(1);
		try
		{
			Entity book1 = dataService.findOneById(authorsAndBooks.getBookMetaData().getName(), BOOK_1);
			Entity author1 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getName(), AUTHOR_1);
			Entity author2 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getName(), AUTHOR_2);

			book1.set(BookMetaData1.AUTHOR, author2);
			dataService.update(book1.getEntityMetaData().getName(), Stream.of(book1));

			Entity author1RetrievedAgain = dataService
					.findOneById(authorsAndBooks.getAuthorMetaData().getName(), author1.getIdValue());
			assertEquals(Collections.emptyList(),
					Lists.newArrayList(author1RetrievedAgain.getEntities(AuthorMetaData1.ATTR_BOOKS)));

			Entity author2Retrieved = dataService
					.findOneById(authorsAndBooks.getAuthorMetaData().getName(), author2.getIdValue());
			Iterable<Entity> author2Books = author2Retrieved.getEntities(AuthorMetaData1.ATTR_BOOKS);

			// expected behavior: book.author changed, new author.books order is undefined
			Set<Object> retrievedAuthor2BookIds = StreamSupport.stream(author2Books.spliterator(), false)
					.map(Entity::getIdValue).collect(toSet());
			assertEquals(retrievedAuthor2BookIds, newHashSet(BOOK_2, BOOK_1));
		}
		finally
		{
			dataService.deleteAll(authorsAndBooks.getBookMetaData().getName());
			dataService.deleteAll(authorsAndBooks.getAuthorMetaData().getName());
		}
	}

	@Test(singleThreaded = true)
	@Transactional
	public void testL1OneToManyEntitySingleEntityDelete()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(1);
		Entity book1 = dataService.findOneById(authorsAndBooks.getBookMetaData().getName(), BOOK_1);
		Entity author1 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getName(), AUTHOR_1);

		dataService.delete(book1.getEntityMetaData().getName(), book1);

		Entity author1RetrievedAgain = dataService
				.findOneById(authorsAndBooks.getAuthorMetaData().getName(), author1.getIdValue());
		assertEquals(Collections.emptyList(),
				Lists.newArrayList(author1RetrievedAgain.getEntities(AuthorMetaData1.ATTR_BOOKS)));

		dataService.deleteAll(authorsAndBooks.getBookMetaData().getName());
		dataService.deleteAll(authorsAndBooks.getAuthorMetaData().getName());
	}

	@Test(singleThreaded = true)
	@Transactional
	public void testL1OneToManyEntityStreamingEntityDelete()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(1);
		try
		{
			Entity book1 = dataService.findOneById(authorsAndBooks.getBookMetaData().getName(), BOOK_1);
			Entity author1 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getName(), AUTHOR_1);

			dataService.delete(book1.getEntityMetaData().getName(), Stream.of(book1));

			Entity author1RetrievedAgain = dataService
					.findOneById(authorsAndBooks.getAuthorMetaData().getName(), author1.getIdValue());
			assertEquals(Collections.emptyList(),
					Lists.newArrayList(author1RetrievedAgain.getEntities(AuthorMetaData1.ATTR_BOOKS)));
		}
		finally
		{
			dataService.deleteAll(authorsAndBooks.getBookMetaData().getName());
			dataService.deleteAll(authorsAndBooks.getAuthorMetaData().getName());
		}
	}

	@Test(singleThreaded = true, expectedExceptions = MolgenisDataException.class)
	public void testOneToManyAuthorRequiredSetBooksNull()
	{
		// FIXME doesn't throw exception
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(2); // book.author required
		Entity author = authorsAndBooks.getAuthors().get(0);
		author.set(ATTR_BOOKS, null);
		dataService.update(authorsAndBooks.getAuthorMetaData().getName(), author);
	}

	@Test(singleThreaded = true, expectedExceptions = MolgenisDataException.class)
	public void testOneToManyAuthorRequiredSetAuthorsNull()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(2); // book.author required
		Entity book = authorsAndBooks.getBooks().get(0);
		book.set(ATTR_AUTHOR, null);
		dataService.update(authorsAndBooks.getBookMetaData().getName(), book);
	}

	@Test(singleThreaded = true)
	public void testOneToManyAuthorRequiredUpdateValue()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(2); // book.author required
		String bookName = authorsAndBooks.getBookMetaData().getName();
		String authorName = authorsAndBooks.getAuthorMetaData().getName();

		Entity book = dataService.findOneById(bookName, BOOK_1);
		book.set(ATTR_AUTHOR, dataService.findOneById(authorName, AUTHOR_2));
		dataService.update(bookName, book);

		Entity updatedBook = dataService.findOneById(bookName, BOOK_1);
		assertEquals(updatedBook.getEntity(ATTR_AUTHOR).getIdValue(), AUTHOR_2);

		// expected behavior: book.author changed, new author.books order is undefined
		Entity updatedAuthor1 = dataService.findOneById(authorName, AUTHOR_1);
		assertEquals(StreamSupport.stream(updatedAuthor1.getEntities(ATTR_BOOKS).spliterator(), false)
				.map(Entity::getIdValue).collect(toSet()), newHashSet());

		Entity updatedAuthor2 = dataService.findOneById(authorName, AUTHOR_2);
		assertEquals(StreamSupport.stream(updatedAuthor2.getEntities(ATTR_BOOKS).spliterator(), false)
				.map(Entity::getIdValue).collect(toSet()), newHashSet(BOOK_2, BOOK_1));
	}

	@Test(singleThreaded = true, expectedExceptions = MolgenisDataException.class)
	public void testOneToManyBooksRequiredSetAuthorNull()
	{
		// FIXME doesn't throw exception
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(3); // author.books required
		String bookName = authorsAndBooks.getBookMetaData().getName();

		Entity book = dataService.findOneById(bookName, BOOK_1);
		book.set(ATTR_AUTHOR, null);
		dataService.update(bookName, book);
	}

	@Test(singleThreaded = true, expectedExceptions = MolgenisDataException.class)
	public void testOneToManyBooksRequiredSetBooksNull()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(3); // author.books required
		String authorName = authorsAndBooks.getAuthorMetaData().getName();

		Entity author = dataService.findOneById(authorName, AUTHOR_1);
		author.set(ATTR_BOOKS, null);
		dataService.update(authorName, author);
	}

	@Test(singleThreaded = true)
	public void testOneToManyBookRequiredUpdateValue()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(3); // book.author required
		String bookName = authorsAndBooks.getBookMetaData().getName();
		String authorName = authorsAndBooks.getAuthorMetaData().getName();

		Entity author1 = dataService.findOneById(authorName, AUTHOR_1);
		author1.set(ATTR_BOOKS, newArrayList(dataService.findOneById(bookName, BOOK_2)));
		Entity author2 = dataService.findOneById(authorName, AUTHOR_2);
		author2.set(ATTR_BOOKS, newArrayList(dataService.findOneById(bookName, BOOK_1)));
		dataService.update(authorName, Stream.of(author1, author2));

		Entity updatedAuthor = dataService.findOneById(authorName, AUTHOR_1);
		assertEquals(updatedAuthor.getEntities(ATTR_BOOKS).iterator().next().getIdValue(), BOOK_2);

		Entity updatedBook1 = dataService.findOneById(bookName, BOOK_1);
		assertEquals(updatedBook1.getEntity(ATTR_AUTHOR).getIdValue(), AUTHOR_2);

		Entity updatedBook2 = dataService.findOneById(bookName, BOOK_2);
		assertEquals(updatedBook2.getEntity(ATTR_AUTHOR).getIdValue(), AUTHOR_1);
	}

	@Test(singleThreaded = true, expectedExceptions = MolgenisDataException.class)
	public void testOneToManyBookAndAuthorRequiredSetAuthorNull()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(4);
		String bookName = authorsAndBooks.getBookMetaData().getName();

		Entity book = dataService.findOneById(bookName, BOOK_1);
		book.set(ATTR_AUTHOR, null);
		dataService.update(bookName, book);
	}

	@Test(singleThreaded = true, expectedExceptions = MolgenisDataException.class)
	public void testOneToManyBookAndAuthorRequiredSetBooksNull()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(4);
		String authorName = authorsAndBooks.getAuthorMetaData().getName();

		Entity author = dataService.findOneById(authorName, AUTHOR_1);
		author.set(ATTR_BOOKS, null);
		dataService.update(authorName, author);
	}

	@Test(singleThreaded = true)
	public void testOneToManyBookAndAuthorRequiredUpdateValue()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(4);
		String bookName = authorsAndBooks.getBookMetaData().getName();
		String authorName = authorsAndBooks.getAuthorMetaData().getName();

		Entity author1 = dataService.findOneById(authorName, AUTHOR_1);
		Entity author2 = dataService.findOneById(authorName, AUTHOR_2);
		Entity book1 = dataService.findOneById(bookName, BOOK_1);
		Entity book2 = dataService.findOneById(bookName, BOOK_2);
		book1.set(ATTR_AUTHOR, author2); // switch authors
		book2.set(ATTR_AUTHOR, author1);

		dataService.update(bookName, Stream.of(book1, book2));

		assertEquals(dataService.findOneById(bookName, BOOK_1).getEntity(ATTR_AUTHOR).getIdValue(), AUTHOR_2);
		assertEquals(dataService.findOneById(bookName, BOOK_2).getEntity(ATTR_AUTHOR).getIdValue(), AUTHOR_1);

		Entity updatedAuthor1 = dataService.findOneById(authorName, AUTHOR_1);
		assertEquals(StreamSupport.stream(updatedAuthor1.getEntities(ATTR_BOOKS).spliterator(), false)
				.map(Entity::getIdValue).collect(toSet()), newHashSet(BOOK_2));

		Entity updatedAuthor2 = dataService.findOneById(authorName, AUTHOR_2);
		assertEquals(StreamSupport.stream(updatedAuthor2.getEntities(ATTR_BOOKS).spliterator(), false)
				.map(Entity::getIdValue).collect(toSet()), newHashSet(BOOK_1));
	}

	@Test(singleThreaded = true)
	public void testOneToManyParentRequiredSetChildrenNull()
	{

	}

	@Test(singleThreaded = true)
	public void testOneToManyParentRequiredSetParentNull()
	{}

	@Test(singleThreaded = true)
	public void testOneToManyParentRequiredUpdateValue()
	{}

	@Test(singleThreaded = true)
	public void testOneToManyChildrenRequiredSetChildrenNull()
	{}

	@Test(singleThreaded = true)
	public void testOneToManyChildrenRequiredSetParentNull()
	{}

	@Test(singleThreaded = true)
	public void testOneToManyChildrenRequiredUpdateValue()
	{}

	@Test(singleThreaded = true)
	public void testOneToManyParentAndChildrenRequiredSetChildrenNull()
	{}

	@Test(singleThreaded = true)
	public void testOneToManyParentAndChildrenRequiredSetParentNull()
	{}

	@Test(singleThreaded = true)
	public void testOneToManyParentAndChildrenRequiredSetUpdateValue()
	{}

	private void importBooksThenAuthors(OneToManyTestHarness.AuthorsAndBooks authorsAndBooks)
	{
		runAsSystem(() ->
		{
			dataService.add(authorsAndBooks.getBookMetaData().getName(), authorsAndBooks.getBooks().stream());
			dataService.add(authorsAndBooks.getAuthorMetaData().getName(), authorsAndBooks.getAuthors().stream());
			waitForIndexToBeStable(authorsAndBooks.getAuthorMetaData().getName(), indexService, LOG);
			waitForIndexToBeStable(authorsAndBooks.getBookMetaData().getName(), indexService, LOG);
		});
	}

	private void importAuthorsThenBooks(OneToManyTestHarness.AuthorsAndBooks authorsAndBooks)
	{
		runAsSystem(() ->
		{
			dataService.add(authorsAndBooks.getAuthorMetaData().getName(), authorsAndBooks.getAuthors().stream());
			dataService.add(authorsAndBooks.getBookMetaData().getName(), authorsAndBooks.getBooks().stream());
			waitForIndexToBeStable(authorsAndBooks.getAuthorMetaData().getName(), indexService, LOG);
			waitForIndexToBeStable(authorsAndBooks.getBookMetaData().getName(), indexService, LOG);
		});
	}

	private void deleteBooksThenAuthors(int testCase)
	{
		dataService.deleteAll("sys_Book" + testCase);
		dataService.deleteAll("sys_Author" + testCase);
	}

	private void deleteAuthorsThenBooks(int testCase)
	{
		dataService.deleteAll("sys_Author" + testCase);
		dataService.deleteAll("sys_Book" + testCase);
	}

	private OneToManyTestHarness.AuthorsAndBooks importAuthorsAndBooks(int testCase)
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks;
		switch (testCase)
		{
			case 1:
				// case 1: books/authors both nillable, order of import not important
				authorsAndBooks = oneToManyTestHarness.createAuthorAndBookEntities(1);
				importBooksThenAuthors(authorsAndBooks);
				return authorsAndBooks;
			case 2:
				// case 2: book.author required so add Author entities first
				authorsAndBooks = oneToManyTestHarness.createAuthorAndBookEntities(2);
				importAuthorsThenBooks(authorsAndBooks);
				return authorsAndBooks;
			case 3:
				// case 3: author.books required so add Book entities first
				authorsAndBooks = oneToManyTestHarness.createAuthorAndBookEntities(3);
				importBooksThenAuthors(authorsAndBooks);
				return authorsAndBooks;
			case 4:
				// case 4: books/authors both required
				authorsAndBooks = oneToManyTestHarness.createAuthorAndBookEntities(4);
				importBooksThenAuthors(authorsAndBooks);
				return authorsAndBooks;
			case 5:
				// case 5: books/authors both nillable, ascending order
				authorsAndBooks = oneToManyTestHarness.createAuthorAndBookEntities(5);
				importBooksThenAuthors(authorsAndBooks);
				return authorsAndBooks;
			case 6:
				// case 6: books/authors both nillable, descending order
				authorsAndBooks = oneToManyTestHarness.createAuthorAndBookEntities(6);
				importBooksThenAuthors(authorsAndBooks);
				return authorsAndBooks;
			default:
				return null;
		}
	}

	private List<Entity> importPersons(int testCase)
	{
		List<Entity> persons = oneToManyTestHarness.createPersonEntities(testCase);
		runAsSystem(() ->
		{
			dataService.add(persons.get(0).getEntityMetaData().getName(), persons.stream());
			waitForIndexToBeStable(persons.get(0).getEntityMetaData().getName(), indexService, LOG);
		});
		return persons;
	}
}
