package org.molgenis.integrationtest.data.abstracts;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.Stream.generate;
import static java.util.stream.Stream.of;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.molgenis.data.Entity;
import org.molgenis.data.EntityListener;
import org.molgenis.data.Fetch;
import org.molgenis.data.Repository;
import org.molgenis.data.RepositoryCapability;
import org.molgenis.data.Sort;
import org.molgenis.data.UnknownEntityException;
import org.molgenis.data.meta.EntityMetaData;
import org.molgenis.data.support.DefaultEntity;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.support.QueryImpl;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;

import com.google.common.collect.Iterators;

public abstract class AbstractDataServiceIT extends AbstractDataIntegrationIT
{
	private static final String ENTITY_NAME = "test_TestEntity";
	private static final String REF_ENTITY_NAME = "test_TestRefEntity";
	private EntityMetaData entityMetaData;
	private EntityMetaData refEntityMetaData;

	private static final String ATTR_ID = "id_attr";
	private static final String ATTR_STRING = "string_attr";
	private static final String ATTR_BOOL = "bool_attr";
	private static final String ATTR_CATEGORICAL = "categorical_attr";
	private static final String ATTR_CATEGORICAL_MREF = "categorical_mref_attr";
	private static final String ATTR_DATE = "date_attr";
	private static final String ATTR_DATETIME = "datetime_attr";
	private static final String ATTR_DECIMAL = "decimal_attr";
	private static final String ATTR_HTML = "html_attr";
	private static final String ATTR_HYPERLINK = "hyperlink_attr";
	private static final String ATTR_LONG = "long_attr";
	private static final String ATTR_INT = "int_attr";
	private static final String ATTR_SCRIPT = "script_attr";
	private static final String ATTR_EMAIL = "email_attr";
	private static final String ATTR_XREF = "xref_attr";
	private static final String ATTR_MREF = "mref_attr";
	private static final String ATTR_REF_ID = "ref_id_attr";
	private static final String ATTR_REF_STRING = "ref_string_attr";
	private DefaultEntity entity1;
	private DefaultEntity entity2;
	private DefaultEntity entity3;
	private DefaultEntity entity4;
	private DefaultEntity entity5;
	private DefaultEntity entity6;
	private DefaultEntity entity7;
	private DefaultEntity entity8;
	private DefaultEntity entity9;
	private DefaultEntity entity10;
	private DefaultEntity refEntity1;
	private DefaultEntity refEntity2;
	private DefaultEntity refEntity3;
	private DefaultEntity refEntity4;
	private DefaultEntity refEntity5;
	private DefaultEntity refEntity6;

	@BeforeClass
	public void setUp()
	{
		//		Package p = null; //new Package("test"); // FIXME
		//		refEntityMetaData = new EntityMetaData("TestRefEntity", p);
		//		refEntityMetaData.addAttribute(ATTR_REF_ID, ROLE_ID).setNillable(false);
		//		refEntityMetaData.addAttribute(ATTR_REF_STRING).setNillable(true).setDataType(MolgenisFieldTypes.STRING);
		//
		//		entityMetaData = new EntityMetaData("TestEntity", p);
		//		entityMetaData.addAttribute(ATTR_ID, ROLE_ID).setNillable(false).setAuto(true);
		//		entityMetaData.addAttribute(ATTR_STRING).setNillable(true).setDataType(MolgenisFieldTypes.STRING);
		//		entityMetaData.addAttribute(ATTR_BOOL).setNillable(true).setDataType(MolgenisFieldTypes.BOOL);
		//		entityMetaData.addAttribute(ATTR_CATEGORICAL).setNillable(true).setDataType(MolgenisFieldTypes.CATEGORICAL)
		//				.setRefEntity(refEntityMetaData);
		//		entityMetaData.addAttribute(ATTR_CATEGORICAL_MREF).setNillable(true)
		//				.setDataType(MolgenisFieldTypes.CATEGORICAL_MREF).setRefEntity(refEntityMetaData);
		//		entityMetaData.addAttribute(ATTR_DATE).setNillable(true).setDataType(MolgenisFieldTypes.DATE);
		//		entityMetaData.addAttribute(ATTR_DATETIME).setNillable(true).setDataType(MolgenisFieldTypes.DATETIME);
		//		entityMetaData.addAttribute(ATTR_EMAIL).setNillable(true).setDataType(MolgenisFieldTypes.EMAIL);
		//		entityMetaData.addAttribute(ATTR_DECIMAL).setNillable(true).setDataType(MolgenisFieldTypes.DECIMAL);
		//		entityMetaData.addAttribute(ATTR_HTML).setNillable(true).setDataType(MolgenisFieldTypes.HTML);
		//		entityMetaData.addAttribute(ATTR_HYPERLINK).setNillable(true).setDataType(MolgenisFieldTypes.HYPERLINK);
		//		entityMetaData.addAttribute(ATTR_LONG).setNillable(true).setDataType(MolgenisFieldTypes.LONG);
		//		entityMetaData.addAttribute(ATTR_INT).setNillable(true).setDataType(MolgenisFieldTypes.INT);
		//		entityMetaData.addAttribute(ATTR_SCRIPT).setNillable(true).setDataType(MolgenisFieldTypes.SCRIPT);
		//		entityMetaData.addAttribute(ATTR_XREF).setNillable(true).setDataType(MolgenisFieldTypes.XREF)
		//				.setRefEntity(refEntityMetaData);
		//		entityMetaData.addAttribute(ATTR_MREF).setNillable(true).setDataType(MolgenisFieldTypes.MREF)
		//				.setRefEntity(refEntityMetaData);
		//
		//		metaDataService.addEntityMeta(refEntityMetaData);
		//		metaDataService.addEntityMeta(entityMetaData);
	}

	@AfterMethod
	public void afterMethod()
	{
		dataService.deleteAll(ENTITY_NAME);
	}

	public void testAdd()
	{
		List<Entity> entities = create(9);
		dataService.add(ENTITY_NAME, entities.stream());
		assertCount(9);
		assertPresent(entities);
	}

	public void testEntityListener()
	{
		List<Entity> entities = create(2);
		dataService.add(ENTITY_NAME, entities.stream());

		AtomicInteger updateCalled = new AtomicInteger(0);
		EntityListener listener = new EntityListener()
		{
			@Override
			public Object getEntityId()
			{
				return entities.get(0).getIdValue();
			}

			@Override
			public void postUpdate(Entity entity)
			{
				updateCalled.incrementAndGet();
				assertEquals(entity.getIdValue(), entities.get(0).getIdValue());
			}
		};

		try
		{
			dataService.addEntityListener(ENTITY_NAME, listener);
			dataService.update(ENTITY_NAME, entities.stream());
			assertEquals(updateCalled.get(), 1);
		}
		finally
		{
			dataService.removeEntityListener(ENTITY_NAME, listener);
			updateCalled.set(0);
			dataService.update(ENTITY_NAME, entities.stream());
			assertEquals(updateCalled.get(), 0);
		}
	}

	public void testCount()
	{
		List<Entity> entities = create(3);
		dataService.add(ENTITY_NAME, entities.stream());
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl<>()), 3);
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl<>().eq(ATTR_ID, entities.get(0).getIdValue())), 1);
	}

	public void testDelete()
	{
		Entity entity = create();
		dataService.add(ENTITY_NAME, entity);
		assertPresent(entity);

		dataService.delete(ENTITY_NAME, entity);
		assertNull(dataService.findOneById(ENTITY_NAME, entity.getIdValue()));
	}

	public void testDeleteById()
	{
		Entity entity = create();
		dataService.add(ENTITY_NAME, entity);
		assertPresent(entity);

		dataService.deleteById(ENTITY_NAME, entity.getIdValue());
		assertNull(dataService.findOneById(ENTITY_NAME, entity.getIdValue()));
	}

	public void testDeleteStream()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities.stream());
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl<>()), entities.size());

		dataService.delete(ENTITY_NAME, entities.stream());
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl<>()), 0);
	}

	public void testDeleteAll()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities.stream());
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl<>()), entities.size());

		dataService.deleteAll(ENTITY_NAME);
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl<>()), 0);
	}

	public void testFindAllEmpty()
	{
		Stream<Entity> retrieved = dataService.findAll(ENTITY_NAME);
		assertEquals(retrieved.count(), 0);
	}

	public void testFindAll()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities.stream());
		Stream<Entity> retrieved = dataService.findAll(ENTITY_NAME);
		assertEquals(retrieved.count(), entities.size());
	}

	public void testFindAllTyped()
	{
		//		List<Entity> entities = create(1);
		//		dataService.add(ENTITY_NAME, entities.stream());
		//		Supplier<Stream<TestEntity>> retrieved = () -> dataService.findAll(ENTITY_NAME, TestEntity.class);
		//		assertEquals(retrieved.get().count(), 1);
		//		assertEquals(retrieved.get().iterator().next().getId(), entities.get(0).getIdValue());
	}

	public void testFindAllByIds()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities.stream());
		Stream<Object> ids = Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(ENTITY_NAME, ids);
		assertEquals(retrieved.count(), entities.size());
	}

	public void testFindAllByIdsTyped()
	{
		//		List<Entity> entities = create(5);
		//		dataService.add(ENTITY_NAME, entities.stream());
		//
		//		Supplier<Stream<TestEntity>> retrieved = () -> dataService.findAll(ENTITY_NAME,
		//				Stream.concat(entities.stream().map(Entity::getIdValue), of("bogus")), TestEntity.class);
		//		assertEquals(retrieved.get().count(), entities.size());
		//		assertEquals(retrieved.get().iterator().next().getId(), entities.get(0).getIdValue());
	}

	public void testFindAllStreamFetch()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities.stream());
		Stream<Object> ids = concat(entities.stream().map(Entity::getIdValue), of("bogus"));
		Stream<Entity> retrieved = dataService.findAll(ENTITY_NAME, ids, new Fetch().field(ATTR_ID));
		assertEquals(retrieved.count(), entities.size());
	}

	public void testFindQuery()
	{
		List<Entity> entities = create(5);
		dataService.add(ENTITY_NAME, entities.stream());
		Supplier<Stream<Entity>> found = () -> dataService.findAll(ENTITY_NAME,
				new QueryImpl<>().eq(ATTR_ID, entities.get(0).getIdValue()));
		assertEquals(found.get().count(), 1);
		assertEquals(found.get().findFirst().get().getIdValue(), entities.get(0).getIdValue());
	}

	public void testFindQueryLimit2_Offset2_sortOnInt()
	{
		dataService.add(REF_ENTITY_NAME, createTestRefEntities().stream());
		dataService.add(ENTITY_NAME, createTestEntities().stream());
		Supplier<Stream<Entity>> found = () -> dataService.findAll(ENTITY_NAME,
				new QueryImpl<>().pageSize(2).offset(2).sort(new Sort(ATTR_INT)));
		assertEquals(found.get().count(), 2);
		assertTrue(found.get().collect(Collectors.toList()).containsAll(Arrays.asList(entity1,entity10)));
	}

	public void testFindQueryTyped()
	{
		//		List<Entity> entities = create(5);
		//		dataService.add(ENTITY_NAME, entities.stream());
		//		Supplier<Stream<TestEntity>> found = () -> dataService.findAll(ENTITY_NAME,
		//				new QueryImpl<TestEntity>().eq(ATTR_ID, entities.get(0).getIdValue()), TestEntity.class);
		//		assertEquals(found.get().count(), 1);
		//		assertEquals(found.get().findFirst().get().getId(), entities.get(0).getIdValue());
	}

	public void testFindOne()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities.stream());
		assertNotNull(dataService.findOneById(ENTITY_NAME, entities.get(0).getIdValue()));
	}

	public void testFindOneTyped()
	{
		//		List<Entity> entities = create(1);
		//		dataService.add(ENTITY_NAME, entities.stream());
		//		TestEntity testEntity = dataService.findOneById(ENTITY_NAME, entities.get(0).getIdValue(), TestEntity.class);
		//		assertNotNull(testEntity);
		//		assertEquals(testEntity.getId(), entities.get(0).getIdValue());
	}

	public void testFindOneFetch()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities.stream());
		assertNotNull(dataService.findOneById(ENTITY_NAME, entities.get(0).getIdValue(), new Fetch().field(ATTR_ID)));
	}

	public void testFindOneFetchTyped()
	{
		//		List<Entity> entities = create(1);
		//		dataService.add(ENTITY_NAME, entities.stream());
		//		TestEntity testEntity = dataService.findOneById(ENTITY_NAME, entities.get(0).getIdValue(),
		//				new Fetch().field(ATTR_ID), TestEntity.class);
		//		assertNotNull(testEntity);
		//		assertEquals(testEntity.getId(), entities.get(0).getIdValue());
	}

	public void testFindOneQuery()
	{
		List<Entity> entities = create(1);
		dataService.add(ENTITY_NAME, entities.stream());
		Entity entity = dataService.findOne(ENTITY_NAME, new QueryImpl<>().eq(ATTR_ID, entities.get(0).getIdValue()));
		assertNotNull(entity);
	}

	public void testFindOneQueryTyped()
	{
		//		List<Entity> entities = create(1);
		//		dataService.add(ENTITY_NAME, entities.stream());
		//		TestEntity entity = dataService.findOne(ENTITY_NAME, new QueryImpl<TestEntity>().eq(ATTR_ID, entities.get(0).getIdValue()),
		//				TestEntity.class);
		//		assertNotNull(entity);
		//		assertEquals(entity.getId(), entities.get(0).getIdValue());
	}

	public void testGetCapabilities()
	{
		Set<RepositoryCapability> capabilities = dataService.getCapabilities(ENTITY_NAME);
		assertNotNull(capabilities);
		assertTrue(capabilities.containsAll(getExpectedCapabilities()));
	}

	public void testGetEntityMetaData()
	{
		EntityMetaData emd = dataService.getEntityMetaData(ENTITY_NAME);
		assertNotNull(emd);
		assertEquals(emd, entityMetaData);
	}

	public void testGetEntityNames()
	{
		Stream<String> names = dataService.getEntityNames();
		assertNotNull(names);
		assertTrue(names.filter(ENTITY_NAME::equals).findFirst().isPresent());
	}

	public void testGetMeta()
	{
		assertNotNull(dataService.getMeta());
	}

	public void testGetRepository()
	{
		Repository repo = dataService.getRepository(ENTITY_NAME);
		assertNotNull(repo);
		assertEquals(repo.getName(), ENTITY_NAME);

		try
		{
			dataService.getRepository("bogus");
			fail("Should have thrown UnknownEntityException");
		}
		catch (UnknownEntityException e)
		{
			// Expected
		}
	}

	public void testHasRepository()
	{
		assertTrue(dataService.hasRepository(ENTITY_NAME));
		assertFalse(dataService.hasRepository("bogus"));
	}

	public void testIterator()
	{
		assertNotNull(dataService.iterator());
		assertTrue(Iterators.contains(dataService.iterator(), dataService.getRepository(ENTITY_NAME)));
	}

	public void testQuery()
	{
		assertNotNull(dataService.query(ENTITY_NAME));
		try
		{
			dataService.query("bogus");
			fail("Should have thrown UnknownEntityException");
		}
		catch (UnknownEntityException e)
		{
			// Expected
		}
	}

	public void testUpdate()
	{
		Entity entity = create(1).get(0);
		dataService.add(ENTITY_NAME, entity);
		entity = dataService.findOneById(ENTITY_NAME, entity.getIdValue());
		assertNotNull(entity);
		assertNull(entity.get(ATTR_STRING));

		entity.set(ATTR_STRING, "qwerty");
		dataService.update(ENTITY_NAME, entity);
		entity = dataService.findOneById(ENTITY_NAME, entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "qwerty");
	}

	public void testUpdateStream()
	{
		Entity entity = create(1).get(0);
		dataService.add(ENTITY_NAME, entity);
		entity = dataService.findOneById(ENTITY_NAME, entity.getIdValue());
		assertNotNull(entity);
		assertNull(entity.get(ATTR_STRING));

		entity.set(ATTR_STRING, "qwerty");
		dataService.update(ENTITY_NAME, of(entity));
		entity = dataService.findOneById(ENTITY_NAME, entity.getIdValue());
		assertNotNull(entity.get(ATTR_STRING));
		assertEquals(entity.get(ATTR_STRING), "qwerty");
	}

	private List<Entity> create(int count)
	{
		return generate(() -> create()).limit(count).collect(toList());
	}

	private Entity create()
	{
		return new DefaultEntity(entityMetaData, dataService);
	}

	private List<Entity> createTestRefEntities()
	{
		List<Entity> entities = new ArrayList<>();
		refEntity1 = new DefaultEntity(refEntityMetaData, dataService);
		refEntity1.set(ATTR_REF_ID, "1");
		refEntity1.set(ATTR_REF_STRING, "refstring1");
		refEntity2 = new DefaultEntity(refEntityMetaData, dataService);
		refEntity2.set(ATTR_REF_ID, "2");
		refEntity2.set(ATTR_REF_STRING, "refstring2");
		refEntity3 = new DefaultEntity(refEntityMetaData, dataService);
		refEntity3.set(ATTR_REF_ID, "3");
		refEntity3.set(ATTR_REF_STRING, "refstring3");
		refEntity4 = new DefaultEntity(refEntityMetaData, dataService);
		refEntity4.set(ATTR_REF_ID, "4");
		refEntity4.set(ATTR_REF_STRING, "refstring4");
		refEntity5 = new DefaultEntity(refEntityMetaData, dataService);
		refEntity5.set(ATTR_REF_ID, "5");
		refEntity5.set(ATTR_REF_STRING, "refstring5");
		refEntity6 = new DefaultEntity(refEntityMetaData, dataService);
		refEntity6.set(ATTR_REF_ID, "6");
		refEntity6.set(ATTR_REF_STRING, "refstring6");

		entities.addAll(Arrays.asList(refEntity1, refEntity2, refEntity3, refEntity4, refEntity5, refEntity6));

		return entities;
	}

	private List<Entity> createTestEntities()
	{
		List<Entity> entities = new ArrayList<>();
		entity1 = new DefaultEntity(entityMetaData, dataService);
		entity1.set(ATTR_STRING, "string1");
		entity1.set(ATTR_BOOL, true);
		entity1.set(ATTR_CATEGORICAL, "1");
		entity1.set(ATTR_CATEGORICAL_MREF, "1");
		entity1.set(ATTR_DATE, "21-12-2012");
		entity1.set(ATTR_DATETIME, "1985-08-12T11:12:13+0500");
		entity1.set(ATTR_EMAIL, "this.is@mail.adress");
		entity1.set(ATTR_DECIMAL, 1.123);
		entity1.set(ATTR_HTML, "<html>where is my head and where is my body</html>");
		entity1.set(ATTR_HYPERLINK, "http://www.molgenis.org");
		entity1.set(ATTR_LONG, 1000000);
		entity1.set(ATTR_INT, 18);
		entity1.set(ATTR_SCRIPT, "/bin/blaat/script.sh");
		entity1.set(ATTR_XREF, "1");
		entity1.set(ATTR_MREF, "1");

		entity2 = new DefaultEntity(entityMetaData, dataService);
		entity2.set(ATTR_STRING, "string1");
		entity2.set(ATTR_BOOL, true);
		entity2.set(ATTR_CATEGORICAL, "1");
		entity2.set(ATTR_CATEGORICAL_MREF, "1");
		entity2.set(ATTR_DATE, "21-12-2012");
		entity2.set(ATTR_DATETIME, "1985-08-12T11:12:13+0500");
		entity2.set(ATTR_EMAIL, "this.is@mail.adress");
		entity2.set(ATTR_DECIMAL, 1.123);
		entity2.set(ATTR_HTML, "<html>where is my head and where is my body</html>");
		entity2.set(ATTR_HYPERLINK, "http://www.molgenis.org");
		entity2.set(ATTR_LONG, 1000000);
		entity2.set(ATTR_INT, 121);
		entity2.set(ATTR_SCRIPT, "/bin/blaat/script.sh");
		entity2.set(ATTR_XREF, "1");
		entity2.set(ATTR_MREF, "1");

		entity3 = new DefaultEntity(entityMetaData, dataService);
		entity3.set(ATTR_STRING, "string1");
		entity3.set(ATTR_BOOL, true);
		entity3.set(ATTR_CATEGORICAL, "1");
		entity3.set(ATTR_CATEGORICAL_MREF, "1");
		entity3.set(ATTR_DATE, "21-12-2012");
		entity3.set(ATTR_DATETIME, "1985-08-12T11:12:13+0500");
		entity3.set(ATTR_EMAIL, "this.is@mail.adress");
		entity3.set(ATTR_DECIMAL, 1.123);
		entity3.set(ATTR_HTML, "<html>where is my head and where is my body</html>");
		entity3.set(ATTR_HYPERLINK, "http://www.molgenis.org");
		entity3.set(ATTR_LONG, 1000000);
		entity3.set(ATTR_INT, 54);
		entity3.set(ATTR_SCRIPT, "/bin/blaat/script.sh");
		entity3.set(ATTR_XREF, "1");
		entity3.set(ATTR_MREF, "1");

		entity4 = new DefaultEntity(entityMetaData, dataService);
		entity4.set(ATTR_STRING, "string1");
		entity4.set(ATTR_BOOL, true);
		entity4.set(ATTR_CATEGORICAL, "1");
		entity4.set(ATTR_CATEGORICAL_MREF, "1");
		entity4.set(ATTR_DATE, "21-12-2012");
		entity4.set(ATTR_DATETIME, "1985-08-12T11:12:13+0500");
		entity4.set(ATTR_EMAIL, "this.is@mail.adress");
		entity4.set(ATTR_DECIMAL, 1.123);
		entity4.set(ATTR_HTML, "<html>where is my head and where is my body</html>");
		entity4.set(ATTR_HYPERLINK, "http://www.molgenis.org");
		entity4.set(ATTR_LONG, 1000000);
		entity4.set(ATTR_INT, 54);
		entity4.set(ATTR_SCRIPT, "/bin/blaat/script.sh");
		entity4.set(ATTR_XREF, "1");
		entity4.set(ATTR_MREF, "1");

		entity5 = new DefaultEntity(entityMetaData, dataService);
		entity5.set(ATTR_STRING, "string1");
		entity5.set(ATTR_BOOL, true);
		entity5.set(ATTR_CATEGORICAL, "1");
		entity5.set(ATTR_CATEGORICAL_MREF, "1");
		entity5.set(ATTR_DATE, "21-12-2012");
		entity5.set(ATTR_DATETIME, "1985-08-12T11:12:13+0500");
		entity5.set(ATTR_EMAIL, "this.is@mail.adress");
		entity5.set(ATTR_DECIMAL, 1.123);
		entity5.set(ATTR_HTML, "<html>where is my head and where is my body</html>");
		entity5.set(ATTR_HYPERLINK, "http://www.molgenis.org");
		entity5.set(ATTR_LONG, 1000000);
		entity5.set(ATTR_INT, 55);
		entity5.set(ATTR_SCRIPT, "/bin/blaat/script.sh");
		entity5.set(ATTR_XREF, "1");
		entity5.set(ATTR_MREF, "1");

		entity6 = new DefaultEntity(entityMetaData, dataService);
		entity6.set(ATTR_STRING, "string1");
		entity6.set(ATTR_BOOL, true);
		entity6.set(ATTR_CATEGORICAL, "1");
		entity6.set(ATTR_CATEGORICAL_MREF, "1");
		entity6.set(ATTR_DATE, "21-12-2012");
		entity6.set(ATTR_DATETIME, "1985-08-12T11:12:13+0500");
		entity6.set(ATTR_EMAIL, "this.is@mail.adress");
		entity6.set(ATTR_DECIMAL, 1.123);
		entity6.set(ATTR_HTML, "<html>where is my head and where is my body</html>");
		entity6.set(ATTR_HYPERLINK, "http://www.molgenis.org");
		entity6.set(ATTR_LONG, 1000000);
		entity6.set(ATTR_INT, 5);
		entity6.set(ATTR_SCRIPT, "/bin/blaat/script.sh");
		entity6.set(ATTR_XREF, "1");
		entity6.set(ATTR_MREF, "1");

		entity7 = new DefaultEntity(entityMetaData, dataService);
		entity7.set(ATTR_STRING, "string1");
		entity7.set(ATTR_BOOL, true);
		entity7.set(ATTR_CATEGORICAL, "1");
		entity7.set(ATTR_CATEGORICAL_MREF, "1");
		entity7.set(ATTR_DATE, "21-12-2012");
		entity7.set(ATTR_DATETIME, "1985-08-12T11:12:13+0500");
		entity7.set(ATTR_EMAIL, "this.is@mail.adress");
		entity7.set(ATTR_DECIMAL, 1.123);
		entity7.set(ATTR_HTML, "<html>where is my head and where is my body</html>");
		entity7.set(ATTR_HYPERLINK, "http://www.molgenis.org");
		entity7.set(ATTR_LONG, 1000000);
		entity7.set(ATTR_INT, 2);
		entity7.set(ATTR_SCRIPT, "/bin/blaat/script.sh");
		entity7.set(ATTR_XREF, "1");
		entity7.set(ATTR_MREF, "1");

		entity8 = new DefaultEntity(entityMetaData, dataService);
		entity8.set(ATTR_STRING, "string1");
		entity8.set(ATTR_BOOL, true);
		entity8.set(ATTR_CATEGORICAL, "1");
		entity8.set(ATTR_CATEGORICAL_MREF, "1");
		entity8.set(ATTR_DATE, "21-12-2012");
		entity8.set(ATTR_DATETIME, "1985-08-12T11:12:13+0500");
		entity8.set(ATTR_EMAIL, "this.is@mail.adress");
		entity8.set(ATTR_DECIMAL, 1.123);
		entity8.set(ATTR_HTML, "<html>where is my head and where is my body</html>");
		entity8.set(ATTR_HYPERLINK, "http://www.molgenis.org");
		entity8.set(ATTR_LONG, 1000000);
		entity8.set(ATTR_INT, 101);
		entity8.set(ATTR_SCRIPT, "/bin/blaat/script.sh");
		entity8.set(ATTR_XREF, "1");
		entity8.set(ATTR_MREF, "1");

		entity9 = new DefaultEntity(entityMetaData, dataService);
		entity9.set(ATTR_STRING, "string1");
		entity9.set(ATTR_BOOL, true);
		entity9.set(ATTR_CATEGORICAL, "1");
		entity9.set(ATTR_CATEGORICAL_MREF, "1");
		entity9.set(ATTR_DATE, "21-12-2012");
		entity9.set(ATTR_DATETIME, "1985-08-12T11:12:13+0500");
		entity9.set(ATTR_EMAIL, "this.is@mail.adress");
		entity9.set(ATTR_DECIMAL, 1.123);
		entity9.set(ATTR_HTML, "<html>where is my head and where is my body</html>");
		entity9.set(ATTR_HYPERLINK, "http://www.molgenis.org");
		entity9.set(ATTR_LONG, 1000000);
		entity9.set(ATTR_INT, 23);
		entity9.set(ATTR_SCRIPT, "/bin/blaat/script.sh");
		entity9.set(ATTR_XREF, "1");
		entity9.set(ATTR_MREF, "1");

		entity10 = new DefaultEntity(entityMetaData, dataService);
		entity10.set(ATTR_STRING, "string1");
		entity10.set(ATTR_BOOL, true);
		entity10.set(ATTR_CATEGORICAL, "1");
		entity10.set(ATTR_CATEGORICAL_MREF, "1");
		entity10.set(ATTR_DATE, "21-12-2012");
		entity10.set(ATTR_DATETIME, "1985-08-12T11:12:13+0500");
		entity10.set(ATTR_EMAIL, "this.is@mail.adress");
		entity10.set(ATTR_DECIMAL, 1.123);
		entity10.set(ATTR_HTML, "<html>where is my head and where is my body</html>");
		entity10.set(ATTR_HYPERLINK, "http://www.molgenis.org");
		entity10.set(ATTR_LONG, 1000000);
		entity10.set(ATTR_INT, 17);
		entity10.set(ATTR_SCRIPT, "/bin/blaat/script.sh");
		entity10.set(ATTR_XREF, "1");
		entity10.set(ATTR_MREF, "1");
		entities.addAll(Arrays.asList(entity1, entity2, entity3, entity4, entity5, entity6, entity7, entity8,
				entity9, entity10));
		return entities;
	}

	private void assertPresent(List<Entity> entities)
	{
		entities.forEach(this::assertPresent);
	}

	private void assertPresent(Entity entity)
	{
		assertNotNull(dataService.findOneById(entityMetaData.getName(), entity.getIdValue()));
	}

	private void assertCount(int count)
	{
		assertEquals(dataService.count(ENTITY_NAME, new QueryImpl<>()), count);
	}

	public abstract List<RepositoryCapability> getExpectedCapabilities();

	public static class TestEntity extends MapEntity
	{
		private static final long serialVersionUID = 1L;

		public String getId()
		{
			return getString(ATTR_ID);
		}

		public void setId(String id)
		{
			set(ATTR_ID, id);
		}
	}
}