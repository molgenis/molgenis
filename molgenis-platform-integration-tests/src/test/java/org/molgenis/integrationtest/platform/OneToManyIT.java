package org.molgenis.integrationtest.platform;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.OneToManyTestHarness;
import org.molgenis.data.index.job.IndexJobScheduler;
import org.molgenis.data.staticentity.bidirectional.authorbook1.AuthorMetaData1;
import org.molgenis.data.staticentity.bidirectional.authorbook1.BookMetaData1;
import org.molgenis.data.staticentity.bidirectional.person1.PersonMetaData1;
import org.molgenis.data.staticentity.bidirectional.person2.PersonMetaData2;
import org.molgenis.data.staticentity.bidirectional.person3.PersonMetaData3;
import org.molgenis.data.staticentity.bidirectional.person4.PersonMetaData4;
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
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.molgenis.data.OneToManyTestHarness.*;
import static org.molgenis.data.OneToManyTestHarness.TestCaseType.*;
import static org.molgenis.data.meta.model.Package.PACKAGE_SEPARATOR;
import static org.molgenis.integrationtest.platform.PlatformIT.*;
import static org.molgenis.security.core.runas.RunAsSystemProxy.runAsSystem;
import static org.testng.Assert.assertEquals;

@ContextConfiguration(classes = { PlatformITConfig.class })
public class OneToManyIT extends AbstractTestNGSpringContextTests
{
	private final Logger LOG = LoggerFactory.getLogger(OneToManyIT.class);

	@Autowired
	private IndexJobScheduler indexService;
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
			authorities.addAll(makeAuthorities("sys" + PACKAGE_SEPARATOR + "Author" + i, true, true, true));
			authorities.addAll(makeAuthorities("sys" + PACKAGE_SEPARATOR + "Book" + i, true, true, true));
			authorities.addAll(makeAuthorities("sys" + PACKAGE_SEPARATOR + "Person" + i, true, true, true));
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
			deleteBooksThenAuthors(1);
			deleteBooksThenAuthors(2);
			deleteBooksThenAuthors(3);
			deleteBooksThenAuthors(4);
			dataService.deleteAll(PersonMetaData1.NAME);
			dataService.deleteAll(PersonMetaData2.NAME);
			dataService.deleteAll(PersonMetaData3.NAME);
			dataService.deleteAll(PersonMetaData4.NAME);
		});
		waitForWorkToBeFinished(indexService, LOG);
	}

	@Test(singleThreaded = true, dataProvider = "allTestCaseDataProvider")
	public void testAuthorAndBookInsert(TestCaseType testCase)
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(testCase);

		String bookEntityName = authorsAndBooks.getBookMetaData().getId();
		assertEquals(dataService.findOneById(bookEntityName, BOOK_1).getEntity(ATTR_AUTHOR).getIdValue(), AUTHOR_1);
		assertEquals(dataService.findOneById(bookEntityName, BOOK_2).getEntity(ATTR_AUTHOR).getIdValue(), AUTHOR_2);
		assertEquals(dataService.findOneById(bookEntityName, BOOK_3).getEntity(ATTR_AUTHOR).getIdValue(), AUTHOR_3);

		String authorEntityName = authorsAndBooks.getAuthorMetaData().getId();
		assertEquals(dataService.findOneById(authorEntityName, AUTHOR_1)
								.getEntities(ATTR_BOOKS)
								.iterator()
								.next()
								.getIdValue(), BOOK_1);
		assertEquals(dataService.findOneById(authorEntityName, AUTHOR_2)
								.getEntities(ATTR_BOOKS)
								.iterator()
								.next()
								.getIdValue(), BOOK_2);
		assertEquals(dataService.findOneById(authorEntityName, AUTHOR_3)
								.getEntities(ATTR_BOOKS)
								.iterator()
								.next()
								.getIdValue(), BOOK_3);
	}

	@Test(singleThreaded = true, dataProvider = "allTestCaseDataProvider")
	public void testPersonInsert(TestCaseType testCase)
	{
		List<Entity> persons = importPersons(testCase);

		String personEntityName = persons.get(0).getEntityType().getId();
		Entity person1 = dataService.findOneById(personEntityName, PERSON_1);
		Entity person2 = dataService.findOneById(personEntityName, PERSON_2);
		Entity person3 = dataService.findOneById(personEntityName, PERSON_3);

		assertEquals(person1.getEntity(ATTR_PARENT).getIdValue(), PERSON_3);
		assertEquals(person2.getEntity(ATTR_PARENT).getIdValue(), PERSON_1);
		assertEquals(person3.getEntity(ATTR_PARENT).getIdValue(), PERSON_2);

		assertEquals(StreamSupport.stream(person1.getEntities(ATTR_CHILDREN).spliterator(), false)
								  .map(Entity::getIdValue)
								  .collect(toSet()), newHashSet(PERSON_2));
		assertEquals(StreamSupport.stream(person2.getEntities(ATTR_CHILDREN).spliterator(), false)
								  .map(Entity::getIdValue)
								  .collect(toSet()), newHashSet(PERSON_3));
		assertEquals(StreamSupport.stream(person3.getEntities(ATTR_CHILDREN).spliterator(), false)
								  .map(Entity::getIdValue)
								  .collect(toSet()), newHashSet(PERSON_1));
	}

	@Test(singleThreaded = true)
	@Transactional
	public void testL1SingleEntityUpdate()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(XREF_NULLABLE);
		try
		{
			Entity book1 = dataService.findOneById(authorsAndBooks.getBookMetaData().getId(), BOOK_1);
			Entity author1 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getId(), AUTHOR_1);
			Entity author2 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getId(), AUTHOR_2);

			book1.set(BookMetaData1.AUTHOR, author2);
			dataService.update(book1.getEntityType().getId(), book1);

			Entity author1RetrievedAgain = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getId(),
					author1.getIdValue());
			assertEquals(Collections.emptyList(),
					Lists.newArrayList(author1RetrievedAgain.getEntities(AuthorMetaData1.ATTR_BOOKS)));

			Entity author2Retrieved = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getId(),
					author2.getIdValue());
			Iterable<Entity> author2Books = author2Retrieved.getEntities(AuthorMetaData1.ATTR_BOOKS);

			// expected behavior: book.author changed, new author.books order is undefined
			Set<Object> retrievedAuthor2BookIds = StreamSupport.stream(author2Books.spliterator(), false)
															   .map(Entity::getIdValue)
															   .collect(toSet());
			assertEquals(retrievedAuthor2BookIds, newHashSet(BOOK_2, BOOK_1));
		}
		finally
		{
			dataService.deleteAll(authorsAndBooks.getBookMetaData().getId());
			dataService.deleteAll(authorsAndBooks.getAuthorMetaData().getId());
		}
	}

	@Test(singleThreaded = true)
	@Transactional
	public void testL1StreamingEntityUpdate()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(XREF_NULLABLE);
		try
		{
			Entity book1 = dataService.findOneById(authorsAndBooks.getBookMetaData().getId(), BOOK_1);
			Entity author1 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getId(), AUTHOR_1);
			Entity author2 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getId(), AUTHOR_2);

			book1.set(BookMetaData1.AUTHOR, author2);
			dataService.update(book1.getEntityType().getId(), Stream.of(book1));

			Entity author1RetrievedAgain = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getId(),
					author1.getIdValue());
			assertEquals(Collections.emptyList(),
					Lists.newArrayList(author1RetrievedAgain.getEntities(AuthorMetaData1.ATTR_BOOKS)));

			Entity author2Retrieved = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getId(),
					author2.getIdValue());
			Iterable<Entity> author2Books = author2Retrieved.getEntities(AuthorMetaData1.ATTR_BOOKS);

			// expected behavior: book.author changed, new author.books order is undefined
			Set<Object> retrievedAuthor2BookIds = StreamSupport.stream(author2Books.spliterator(), false)
															   .map(Entity::getIdValue)
															   .collect(toSet());
			assertEquals(retrievedAuthor2BookIds, newHashSet(BOOK_2, BOOK_1));
		}
		finally
		{
			dataService.deleteAll(authorsAndBooks.getBookMetaData().getId());
			dataService.deleteAll(authorsAndBooks.getAuthorMetaData().getId());
		}
	}

	@Test(singleThreaded = true)
	@Transactional
	public void testL1EntitySingleEntityDelete()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(XREF_NULLABLE);
		try
		{
			Entity book1 = dataService.findOneById(authorsAndBooks.getBookMetaData().getId(), BOOK_1);
			Entity author1 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getId(), AUTHOR_1);

			dataService.delete(book1.getEntityType().getId(), book1);

			Entity author1RetrievedAgain = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getId(),
					author1.getIdValue());
			assertEquals(Collections.emptyList(),
					Lists.newArrayList(author1RetrievedAgain.getEntities(AuthorMetaData1.ATTR_BOOKS)));
		}
		finally
		{
			dataService.deleteAll(authorsAndBooks.getBookMetaData().getId());
			dataService.deleteAll(authorsAndBooks.getAuthorMetaData().getId());
		}
	}

	@Test(singleThreaded = true)
	@Transactional
	public void testL1EntityStreamingEntityDelete()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(XREF_NULLABLE);
		try
		{
			Entity book1 = dataService.findOneById(authorsAndBooks.getBookMetaData().getId(), BOOK_1);
			Entity author1 = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getId(), AUTHOR_1);

			dataService.delete(book1.getEntityType().getId(), Stream.of(book1));

			Entity author1RetrievedAgain = dataService.findOneById(authorsAndBooks.getAuthorMetaData().getId(),
					author1.getIdValue());
			assertEquals(Collections.emptyList(),
					Lists.newArrayList(author1RetrievedAgain.getEntities(AuthorMetaData1.ATTR_BOOKS)));
		}
		finally
		{
			dataService.deleteAll(authorsAndBooks.getBookMetaData().getId());
			dataService.deleteAll(authorsAndBooks.getAuthorMetaData().getId());
		}
	}

	@Test(singleThreaded = true, dataProvider = "allTestCaseDataProvider")
	public void testUpdateAuthorValue(TestCaseType testCase)
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(testCase);
		String bookName = authorsAndBooks.getBookMetaData().getId();
		String authorName = authorsAndBooks.getAuthorMetaData().getId();

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
								  .map(Entity::getIdValue)
								  .collect(toSet()), newHashSet(BOOK_2));

		Entity updatedAuthor2 = dataService.findOneById(authorName, AUTHOR_2);
		assertEquals(StreamSupport.stream(updatedAuthor2.getEntities(ATTR_BOOKS).spliterator(), false)
								  .map(Entity::getIdValue)
								  .collect(toSet()), newHashSet(BOOK_1));
	}

	@Test(singleThreaded = true, dataProvider = "allTestCaseDataProvider")
	public void testUpdateParentValue(TestCaseType testCase)
	{
		List<Entity> persons = importPersons(testCase);
		String personName = persons.get(0).getEntityType().getId();

		Entity person1 = dataService.findOneById(personName, PERSON_1);
		Entity person2 = dataService.findOneById(personName, PERSON_2);
		Entity person3 = dataService.findOneById(personName, PERSON_3);
		person1.set(ATTR_PARENT, person2); // switch parents
		person2.set(ATTR_PARENT, person3);
		person3.set(ATTR_PARENT, person1);
		dataService.update(personName, Stream.of(person1, person2, person3));

		assertEquals(dataService.findOneById(personName, PERSON_1).getEntity(ATTR_PARENT).getIdValue(), PERSON_2);
		assertEquals(dataService.findOneById(personName, PERSON_2).getEntity(ATTR_PARENT).getIdValue(), PERSON_3);
		assertEquals(dataService.findOneById(personName, PERSON_3).getEntity(ATTR_PARENT).getIdValue(), PERSON_1);

		Entity updatedPerson1 = dataService.findOneById(personName, PERSON_1);
		assertEquals(StreamSupport.stream(updatedPerson1.getEntities(ATTR_CHILDREN).spliterator(), false)
								  .map(Entity::getIdValue)
								  .collect(toSet()), newHashSet(PERSON_3));

		Entity updatedPerson2 = dataService.findOneById(personName, PERSON_2);
		assertEquals(StreamSupport.stream(updatedPerson2.getEntities(ATTR_CHILDREN).spliterator(), false)
								  .map(Entity::getIdValue)
								  .collect(toSet()), newHashSet(PERSON_1));

		Entity updatedPerson3 = dataService.findOneById(personName, PERSON_3);
		assertEquals(StreamSupport.stream(updatedPerson3.getEntities(ATTR_CHILDREN).spliterator(), false)
								  .map(Entity::getIdValue)
								  .collect(toSet()), newHashSet(PERSON_2));
	}

	@Test(singleThreaded = true)
	public void testUpdateAuthorOrderAscending()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(ASCENDING_ORDER);
		String bookName = authorsAndBooks.getBookMetaData().getId();
		String authorName = authorsAndBooks.getAuthorMetaData().getId();

		Entity book1 = dataService.findOneById(bookName, BOOK_1);
		Entity book2 = dataService.findOneById(bookName, BOOK_2);
		Entity author3 = dataService.findOneById(authorName, AUTHOR_3);
		book1.set(ATTR_AUTHOR, author3);
		book2.set(ATTR_AUTHOR, author3);
		dataService.update(bookName, Stream.of(book2, book1));

		Entity updatedAuthor3 = dataService.findOneById(authorName, AUTHOR_3);
		assertEquals(StreamSupport.stream(updatedAuthor3.getEntities(ATTR_BOOKS).spliterator(), false)
								  .map(Entity::getIdValue)
								  .collect(toList()), newArrayList(BOOK_1, BOOK_2, BOOK_3));

		Entity updatedAuthor1 = dataService.findOneById(authorName, AUTHOR_1);
		Entity updatedAuthor2 = dataService.findOneById(authorName, AUTHOR_2);
		assertEquals(Iterables.size(updatedAuthor1.getEntities(bookName)), 0);
		assertEquals(Iterables.size(updatedAuthor2.getEntities(bookName)), 0);
	}

	@Test(singleThreaded = true)
	public void testUpdateAuthorOrderDescending()
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks = importAuthorsAndBooks(DESCENDING_ORDER);
		String bookName = authorsAndBooks.getBookMetaData().getId();
		String authorName = authorsAndBooks.getAuthorMetaData().getId();

		Entity book2 = dataService.findOneById(bookName, BOOK_2);
		Entity book3 = dataService.findOneById(bookName, BOOK_3);
		Entity author1 = dataService.findOneById(authorName, AUTHOR_1);
		book2.set(ATTR_AUTHOR, author1);
		book3.set(ATTR_AUTHOR, author1);
		dataService.update(bookName, Stream.of(book2, book3));

		Entity updatedAuthor1 = dataService.findOneById(authorName, AUTHOR_1);
		assertEquals(StreamSupport.stream(updatedAuthor1.getEntities(ATTR_BOOKS).spliterator(), false)
								  .map(Entity::getIdValue)
								  .collect(toList()), newArrayList(BOOK_3, BOOK_2, BOOK_1));

		Entity updatedAuthor2 = dataService.findOneById(authorName, AUTHOR_2);
		Entity updatedAuthor3 = dataService.findOneById(authorName, AUTHOR_3);
		assertEquals(Iterables.size(updatedAuthor2.getEntities(ATTR_BOOKS)), 0);
		assertEquals(Iterables.size(updatedAuthor3.getEntities(ATTR_BOOKS)), 0);
	}

	@Test(singleThreaded = true)
	public void testUpdateParentOrderAscending()
	{
		List<Entity> persons = importPersons(ASCENDING_ORDER);
		String personName = persons.get(0).getEntityType().getId();

		Entity person1 = dataService.findOneById(personName, PERSON_1);
		Entity person2 = dataService.findOneById(personName, PERSON_2);
		Entity person3 = dataService.findOneById(personName, PERSON_3);
		person1.set(ATTR_PARENT, person3);
		person2.set(ATTR_PARENT, person3);
		person3.set(ATTR_PARENT, person3);
		dataService.update(personName, Stream.of(person2, person1, person3));

		Entity updatedPerson3 = dataService.findOneById(personName, PERSON_3);
		assertEquals(StreamSupport.stream(updatedPerson3.getEntities(ATTR_CHILDREN).spliterator(), false)
								  .map(Entity::getIdValue)
								  .collect(toList()), newArrayList(PERSON_1, PERSON_2, PERSON_3));

		Entity updatedPerson1 = dataService.findOneById(personName, PERSON_1);
		Entity updatedPerson2 = dataService.findOneById(personName, PERSON_2);
		assertEquals(Iterables.size(updatedPerson1.getEntities(ATTR_CHILDREN)), 0);
		assertEquals(Iterables.size(updatedPerson2.getEntities(ATTR_CHILDREN)), 0);
	}

	@Test(singleThreaded = true)
	public void testUpdateParentOrderDescending()
	{
		List<Entity> persons = importPersons(DESCENDING_ORDER);
		String personName = persons.get(0).getEntityType().getId();

		Entity person1 = dataService.findOneById(personName, PERSON_1);
		Entity person2 = dataService.findOneById(personName, PERSON_2);
		Entity person3 = dataService.findOneById(personName, PERSON_3);
		person1.set(ATTR_PARENT, person1);
		person2.set(ATTR_PARENT, person1);
		person3.set(ATTR_PARENT, person1);
		dataService.update(personName, Stream.of(person2, person1, person3));

		Entity updatedPerson1 = dataService.findOneById(personName, PERSON_1);
		assertEquals(StreamSupport.stream(updatedPerson1.getEntities(ATTR_CHILDREN).spliterator(), false)
								  .map(Entity::getIdValue)
								  .collect(toList()), newArrayList(PERSON_3, PERSON_2, PERSON_1));

		Entity updatedPerson2 = dataService.findOneById(personName, PERSON_2);
		Entity updatedPerson3 = dataService.findOneById(personName, PERSON_3);
		assertEquals(Iterables.size(updatedPerson2.getEntities(ATTR_CHILDREN)), 0);
		assertEquals(Iterables.size(updatedPerson3.getEntities(ATTR_CHILDREN)), 0);
	}

	private void deleteBooksThenAuthors(int testCase)
	{
		dataService.deleteAll("sys" + PACKAGE_SEPARATOR + "Book" + testCase);
		dataService.deleteAll("sys" + PACKAGE_SEPARATOR + "Author" + testCase);
	}

	private OneToManyTestHarness.AuthorsAndBooks importAuthorsAndBooks(TestCaseType testCase)
	{
		OneToManyTestHarness.AuthorsAndBooks authorsAndBooks;
		authorsAndBooks = oneToManyTestHarness.createAuthorAndBookEntities(testCase);
		runAsSystem(() ->
		{
			dataService.add(authorsAndBooks.getAuthorMetaData().getId(), authorsAndBooks.getAuthors().stream());
			dataService.add(authorsAndBooks.getBookMetaData().getId(), authorsAndBooks.getBooks().stream());
			waitForIndexToBeStable(authorsAndBooks.getAuthorMetaData(), indexService, LOG);
			waitForIndexToBeStable(authorsAndBooks.getBookMetaData(), indexService, LOG);
		});
		return authorsAndBooks;
	}

	private List<Entity> importPersons(TestCaseType testCase)
	{
		List<Entity> persons = oneToManyTestHarness.createPersonEntities(testCase);
		runAsSystem(() ->
		{
			dataService.add(persons.get(0).getEntityType().getId(), persons.stream());
			waitForIndexToBeStable(persons.get(0).getEntityType(), indexService, LOG);
		});
		return persons;
	}

	/**
	 * Serves all test case numbers.
	 */
	@DataProvider(name = "allTestCaseDataProvider")
	private Object[][] allTestCaseDataProvider()
	{
		return new Object[][] { { XREF_NULLABLE }, { XREF_REQUIRED }, { ASCENDING_ORDER }, { DESCENDING_ORDER } };
	}
}
