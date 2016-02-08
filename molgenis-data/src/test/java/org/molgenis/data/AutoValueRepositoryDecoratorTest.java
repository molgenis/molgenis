package org.molgenis.data;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.molgenis.data.EntityMetaData.AttributeRole.ROLE_ID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AutoValueRepositoryDecoratorTest
{
	private static final String ATTR_ID = "id";
	private static final String ATTR_DATE_AUTO_DEFAULT = "date_auto-default";
	private static final String ATTR_DATE_AUTO_FALSE = "date_auto-false";
	private static final String ATTR_DATE_AUTO_TRUE = "date_auto-true";
	private static final String ATTR_DATETIME_AUTO_DEFAULT = "datetime_auto-default";
	private static final String ATTR_DATETIME_AUTO_FALSE = "datetime_auto-false";
	private static final String ATTR_DATETIME_AUTO_TRUE = "datetime_auto-true";

	private DefaultEntityMetaData entityMetaData;
	private Repository decoratedRepository;
	private AutoValueRepositoryDecorator repositoryDecorator;

	@BeforeMethod
	public void setUpBeforeMethod()
	{
		entityMetaData = new DefaultEntityMetaData("entity");
		entityMetaData.addAttribute(ATTR_ID, ROLE_ID);
		entityMetaData.addAttribute(ATTR_DATE_AUTO_DEFAULT).setDataType(MolgenisFieldTypes.DATE);
		entityMetaData.addAttribute(ATTR_DATE_AUTO_FALSE).setDataType(MolgenisFieldTypes.DATE).setAuto(false);
		entityMetaData.addAttribute(ATTR_DATE_AUTO_TRUE).setDataType(MolgenisFieldTypes.DATE).setAuto(true);
		entityMetaData.addAttribute(ATTR_DATETIME_AUTO_DEFAULT).setDataType(MolgenisFieldTypes.DATETIME);
		entityMetaData.addAttribute(ATTR_DATETIME_AUTO_FALSE).setDataType(MolgenisFieldTypes.DATETIME).setAuto(false);
		entityMetaData.addAttribute(ATTR_DATETIME_AUTO_TRUE).setDataType(MolgenisFieldTypes.DATETIME).setAuto(true);
		decoratedRepository = when(mock(Repository.class).getEntityMetaData()).thenReturn(entityMetaData).getMock();
		repositoryDecorator = new AutoValueRepositoryDecorator(decoratedRepository, mock(IdGenerator.class));
	}

	@Test
	public void addEntity()
	{
		Entity entity = new MapEntity(entityMetaData);
		repositoryDecorator.add(entity);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addStream()
	{
		Entity entity0 = new MapEntity(entityMetaData);
		Entity entity1 = new MapEntity(entityMetaData);
		Stream<Entity> entities = Stream.of(entity0, entity1);

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
		assertEquals(repositoryDecorator.add(entities), Integer.valueOf(2));

		validateEntity(entity0);
		validateEntity(entity1);
	}

	@Test
	public void deleteStream()
	{
		Stream<Entity> entities = Stream.of(mock(Entity.class));
		repositoryDecorator.delete(entities);
		verify(decoratedRepository, times(1)).delete(entities);
	}

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	@Test
	public void updateStream()
	{
		Entity entity0 = mock(Entity.class);
		Stream<Entity> entities = Stream.of(entity0);
		ArgumentCaptor<Stream<Entity>> captor = ArgumentCaptor.forClass((Class) Stream.class);
		doNothing().when(decoratedRepository).update(captor.capture());
		repositoryDecorator.update(entities);
		assertEquals(Arrays.asList(entity0), captor.getValue().collect(Collectors.toList()));
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
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
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
		assertEquals(expectedEntities.collect(Collectors.toList()), Arrays.asList(entity0, entity1));
	}

	@Test
	public void findOneObjectFetch()
	{
		Object id = Integer.valueOf(0);
		Fetch fetch = new Fetch();
		Entity entity = mock(Entity.class);
		when(decoratedRepository.findOne(id, fetch)).thenReturn(entity);
		assertEquals(entity, repositoryDecorator.findOne(id, fetch));
		verify(decoratedRepository, times(1)).findOne(id, fetch);
	}

	@Test
	public void findAllAsStream()
	{
		Entity entity0 = mock(Entity.class);
		Query query = mock(Query.class);
		when(decoratedRepository.findAll(query)).thenReturn(Stream.of(entity0));
		Stream<Entity> entities = repositoryDecorator.findAll(query);
		assertEquals(entities.collect(Collectors.toList()), Arrays.asList(entity0));
	}

	private void validateEntity(Entity entity)
	{
		assertNull(entity.getDate(ATTR_DATE_AUTO_DEFAULT));
		assertNull(entity.getDate(ATTR_DATE_AUTO_FALSE));
		assertNotNull(entity.getDate(ATTR_DATE_AUTO_TRUE));
		assertNull(entity.getDate(ATTR_DATETIME_AUTO_DEFAULT));
		assertNull(entity.getDate(ATTR_DATETIME_AUTO_FALSE));
		assertNotNull(entity.getDate(ATTR_DATETIME_AUTO_TRUE));
	}
}
