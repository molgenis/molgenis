package org.molgenis.data;

import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.EntityMetaData;
import org.molgenis.data.populate.IdGenerator;
import org.molgenis.data.support.DynamicEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.molgenis.MolgenisFieldTypes.AttributeType.DATE;
import static org.molgenis.MolgenisFieldTypes.AttributeType.STRING;
import static org.testng.Assert.*;

public class AutoValueRepositoryDecoratorTest
{
	private static final String ATTR_ID = "id";
	private static final String ATTR_DATE_AUTO_DEFAULT = "date_auto-default";
	private static final String ATTR_DATE_AUTO_FALSE = "date_auto-false";
	private static final String ATTR_DATE_AUTO_TRUE = "date_auto-true";
	private static final String ATTR_DATETIME_AUTO_DEFAULT = "datetime_auto-default";
	private static final String ATTR_DATETIME_AUTO_FALSE = "datetime_auto-false";
	private static final String ATTR_DATETIME_AUTO_TRUE = "datetime_auto-true";

	private EntityMetaData entityMeta;
	private Repository<Entity> decoratedRepository;
	private AutoValueRepositoryDecorator repositoryDecorator;
	private IdGenerator idGenerator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityMeta = when(mock(EntityMetaData.class).getName()).thenReturn("entity").getMock();
		Attribute attrId = when(mock(Attribute.class).getName()).thenReturn(ATTR_ID).getMock();
		when(attrId.getDataType()).thenReturn(STRING);
		when(attrId.isAuto()).thenReturn(true);
		Attribute attrDateAutoDefault = when(mock(Attribute.class).getName())
				.thenReturn(ATTR_DATE_AUTO_DEFAULT).getMock();
		when(attrDateAutoDefault.getDataType()).thenReturn(DATE);
		Attribute attrDateAutoFalse = when(mock(Attribute.class).getName())
				.thenReturn(ATTR_DATE_AUTO_FALSE).getMock();
		when(attrDateAutoFalse.getDataType()).thenReturn(DATE);
		when(attrDateAutoFalse.isAuto()).thenReturn(false);
		Attribute attrDateAutoTrue = when(mock(Attribute.class).getName())
				.thenReturn(ATTR_DATE_AUTO_TRUE).getMock();
		when(attrDateAutoTrue.getDataType()).thenReturn(DATE);
		when(attrDateAutoTrue.isAuto()).thenReturn(true);
		Attribute attrDateTimeAutoDefault = when(mock(Attribute.class).getName())
				.thenReturn(ATTR_DATETIME_AUTO_DEFAULT).getMock();
		when(attrDateTimeAutoDefault.getDataType()).thenReturn(DATE);
		Attribute attrDateTimeAutoFalse = when(mock(Attribute.class).getName())
				.thenReturn(ATTR_DATETIME_AUTO_FALSE).getMock();
		when(attrDateTimeAutoFalse.getDataType()).thenReturn(DATE);
		when(attrDateTimeAutoFalse.isAuto()).thenReturn(false);
		Attribute attrDateTimeAutoTrue = when(mock(Attribute.class).getName())
				.thenReturn(ATTR_DATETIME_AUTO_TRUE).getMock();
		when(attrDateTimeAutoTrue.getDataType()).thenReturn(DATE);
		when(attrDateTimeAutoTrue.isAuto()).thenReturn(true);
		when(entityMeta.getIdAttribute()).thenReturn(attrId);
		when(entityMeta.getAttributes()).thenReturn(
				asList(attrId, attrDateAutoDefault, attrDateAutoFalse, attrDateAutoTrue, attrDateTimeAutoDefault,
						attrDateTimeAutoFalse, attrDateTimeAutoTrue));
		when(entityMeta.getAtomicAttributes()).thenReturn(
				asList(attrId, attrDateAutoDefault, attrDateAutoFalse, attrDateAutoTrue, attrDateTimeAutoDefault,
						attrDateTimeAutoFalse, attrDateTimeAutoTrue));
		when(entityMeta.getAttribute(ATTR_ID)).thenReturn(attrId);
		when(entityMeta.getAttribute(ATTR_DATE_AUTO_DEFAULT)).thenReturn(attrDateAutoDefault);
		when(entityMeta.getAttribute(ATTR_DATE_AUTO_FALSE)).thenReturn(attrDateAutoFalse);
		when(entityMeta.getAttribute(ATTR_DATE_AUTO_TRUE)).thenReturn(attrDateAutoTrue);
		when(entityMeta.getAttribute(ATTR_DATETIME_AUTO_DEFAULT)).thenReturn(attrDateTimeAutoDefault);
		when(entityMeta.getAttribute(ATTR_DATETIME_AUTO_FALSE)).thenReturn(attrDateTimeAutoFalse);
		when(entityMeta.getAttribute(ATTR_DATETIME_AUTO_TRUE)).thenReturn(attrDateTimeAutoTrue);
		decoratedRepository = when(mock(Repository.class).getEntityMetaData()).thenReturn(entityMeta).getMock();
		idGenerator = mock(IdGenerator.class);
		Mockito.when(idGenerator.generateId()).thenReturn("ID1").thenReturn("ID2");
		repositoryDecorator = new AutoValueRepositoryDecorator(decoratedRepository, idGenerator);
	}

	@Test
	public void addEntity()
	{
		Entity entity = new DynamicEntity(entityMeta);
		repositoryDecorator.add(entity);
	}

	@Test
	public void addEntityFillsInAutoIdValue()
	{
		Entity entity = new DynamicEntity(entityMeta);
		repositoryDecorator.add(entity);
		ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
		verify(decoratedRepository).add(captor.capture());
		Entity autoValueEntity = captor.getValue();
		assertEquals(autoValueEntity.getIdValue(), "ID1");
	}

	@Test
	public void addEntityDoesntOverrideFilledInIdValue()
	{
		Entity entity = new DynamicEntity(entityMeta);
		entity.set(ATTR_ID, "My ID");
		repositoryDecorator.add(entity);
		ArgumentCaptor<Entity> captor = ArgumentCaptor.forClass(Entity.class);
		verify(decoratedRepository).add(captor.capture());
		Entity autoValueEntity = captor.getValue();
		assertEquals(autoValueEntity.getIdValue(), "My ID");
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addStream()
	{
		Entity entity0 = new DynamicEntity(entityMeta);
		Entity entity1 = new DynamicEntity(entityMeta);
		entity1.set(ATTR_ID, "My ID");
		Entity entity2 = new DynamicEntity(entityMeta);
		Stream<Entity> entities = Stream.of(entity0, entity1, entity2);

		when(decoratedRepository.add(any(Stream.class))).thenAnswer(new Answer<Integer>()
		{
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable
			{
				Stream<Entity> entities = (Stream<Entity>) invocation.getArguments()[0];
				List<Entity> entityList = entities.collect(Collectors.toList());
				return entityList.size();
			}
		});
		assertEquals(repositoryDecorator.add(entities), Integer.valueOf(3));

		validateEntity(entity0);
		assertEquals(entity0.getIdValue(), "ID1");
		validateEntity(entity1);
		assertEquals(entity1.getIdValue(), "My ID");
		validateEntity(entity2);
		assertEquals(entity2.getIdValue(), "ID2");
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> entities = Stream.of(mock(Entity.class));
		repositoryDecorator.delete(entities);
		verify(decoratedRepository, times(1)).delete(entities);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		Entity entity0 = mock(Entity.class);
		Stream<Entity> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		repositoryDecorator.update(entities);
		assertEquals(asList(entity0), captor.getValue().collect(Collectors.toList()));
	}

	@Test
	public void findAllStream()
	{
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = repositoryDecorator.findAll(entityIds);
		assertEquals(expectedEntities.collect(Collectors.toList()), asList(entity0, entity1));
	}

	@Test
	public void findAllStreamFetch()
	{
		Fetch fetch = new Fetch();
		Object id0 = "id0";
		Object id1 = "id1";
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Stream<Object> entityIds = Stream.of(id0, id1);
		when(decoratedRepository.findAll(entityIds, fetch)).thenReturn(Stream.of(entity0, entity1));
		Stream<Entity> expectedEntities = repositoryDecorator.findAll(entityIds, fetch);
		assertEquals(expectedEntities.collect(Collectors.toList()), asList(entity0, entity1));
	}

	@Test
	public void findOneObjectFetch()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		when(decoratedRepository.findOneById(id, fetch)).thenReturn(entity);
		assertEquals(entity, repositoryDecorator.findOneById(id, fetch));
		verify(decoratedRepository, times(1)).findOneById(id, fetch);
	}

	@Test
	public void findAllAsStream()
	{
		Entity entity0 = mock(Entity.class);
		Query<Entity> query = mock(Query.class);
		when(decoratedRepository.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = repositoryDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), asList(entity0));
	}

	@Test
	public void forEachBatchedFetch()
	{
		Fetch fetch = new Fetch();
		Consumer<List<Entity>> consumer = mock(Consumer.class);
		repositoryDecorator.forEachBatched(fetch, consumer, 234);
		verify(decoratedRepository, times(1)).forEachBatched(fetch, consumer, 234);
	}

	private void validateEntity(Entity entity)
	{
		assertNotNull(entity.getIdValue());
		assertNull(entity.getDate(ATTR_DATE_AUTO_DEFAULT));
		assertNull(entity.getDate(ATTR_DATE_AUTO_FALSE));
		assertNotNull(entity.getDate(ATTR_DATE_AUTO_TRUE));
		assertNull(entity.getDate(ATTR_DATETIME_AUTO_DEFAULT));
		assertNull(entity.getDate(ATTR_DATETIME_AUTO_FALSE));
		assertNotNull(entity.getDate(ATTR_DATETIME_AUTO_TRUE));
	}
}
