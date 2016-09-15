package org.molgenis.test.data;

import org.molgenis.data.Entity;
import org.molgenis.data.meta.model.AttributeMetaDataFactory;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.meta.model.EntityMetaDataFactory;
import org.molgenis.data.meta.model.PackageFactory;
import org.molgenis.data.support.DynamicEntity;
import org.molgenis.test.data.staticentity.bidirectional.AuthorMetaData;
import org.molgenis.test.data.staticentity.bidirectional.BookMetaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.stream.Stream;


@Component
public class EntityOneToManyTestHarness
{
	@Autowired
	private PackageFactory packageFactory;

	@Autowired
	private EntityMetaDataFactory entityMetaDataFactory;

	@Autowired
	private AttributeMetaDataFactory attributeMetaDataFactory;

	@Autowired
	private TestPackage testPackage;

	@Autowired
	AuthorMetaData authorMetaData;

	@Autowired
	BookMetaData bookMetaData;

	@PostConstruct
	public void postConstruct()
	{
	}

	public EntityMetaData createAuthorEntityMetaData()
	{
		return authorMetaData;
	}

	public EntityMetaData createBookEntityMetaData()
	{
		return bookMetaData;
	}

	public Stream<Entity> createAuthorEntities()
	{
		Entity author1 = new DynamicEntity(authorMetaData);
		author1.set(AuthorMetaData.ID, "Fabian");
		author1.set(AuthorMetaData.ATTR_BOOKS, null);

		Entity author2 = new DynamicEntity(authorMetaData);
		author2.set(AuthorMetaData.ID, "Henk");
		author2.set(AuthorMetaData.ATTR_BOOKS, null);

		Entity author3 = new DynamicEntity(authorMetaData);
		author3.set(AuthorMetaData.ID, "Mechteld");
		author3.set(AuthorMetaData.ATTR_BOOKS, null);

		return Stream.of(author1, author2, author3);
	}

	public Stream<Entity> createBookEntities()
	{
		Entity book1 = new DynamicEntity(bookMetaData);
		book1.set(BookMetaData.LABEL, "MOLGENIS for Dummies");
		book1.set(BookMetaData.ATTR_BOOKS, null);

		Entity book2 = new DynamicEntity(bookMetaData);
		book2.set(AuthorMetaData.ID, "War and Peace and MOLGENIS");
		book2.set(AuthorMet, null);

		return Stream.of(book1, book2);
	}
}
