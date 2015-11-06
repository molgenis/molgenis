package org.molgenis.data;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

import java.util.Arrays;

import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

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
		entityMetaData.addAttribute(ATTR_ID).setIdAttribute(true).setDataType(MolgenisFieldTypes.STRING);
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

	@Test
	public void addEntityIterable()
	{
		Entity entity0 = new MapEntity(entityMetaData);
		Entity entity1 = new MapEntity(entityMetaData);
		repositoryDecorator.add(Arrays.asList(entity0, entity1));

		validateEntity(entity0);
		validateEntity(entity1);
	}

	@Test
	public void findAllIterableFetch()
	{
		Iterable<Object> ids = Arrays.<Object> asList(Integer.valueOf(0), Integer.valueOf(1));
		Fetch fetch = new Fetch();
		Entity entity0 = mock(Entity.class);
		Entity entity1 = mock(Entity.class);
		Iterable<Entity> entities = Arrays.asList(entity0, entity1);
		when(decoratedRepository.findAll(ids, fetch)).thenReturn(entities);
		assertEquals(Arrays.asList(entity0, entity1), Lists.newArrayList(repositoryDecorator.findAll(ids, fetch)));
		verify(decoratedRepository, times(1)).findAll(ids, fetch);
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
