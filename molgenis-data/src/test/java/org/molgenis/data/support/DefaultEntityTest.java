package org.molgenis.data.support;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.Lists;

public class DefaultEntityTest
{
	private DefaultEntityMetaData emd;
	private DefaultEntity entity;
	private Date utilDate = new Date();
	@Mock
	DataService dataService;

	@BeforeMethod
	public void beforeMethod()
	{
		MockitoAnnotations.initMocks(this);
		emd = new DefaultEntityMetaData("Entity");
		emd.addAttributeMetaData(new DefaultAttributeMetaData("id").setIdAttribute(true));
		emd.addAttributeMetaData(new DefaultAttributeMetaData("xdatetime", FieldTypeEnum.DATE_TIME));
		entity = new DefaultEntity(emd, dataService);
		entity.set("xdatetime", utilDate);
	}

	@Test
	public void testGetForAttributeWithTypeDateTime()
	{
		Object actual = entity.get("xdatetime");
		Assert.assertEquals(Date.class, actual.getClass());
		assertEquals(utilDate, actual);
	}

	@Test
	public void testGetUtilDateForAttributeWithTypeDateTime()
	{
		Object actual = entity.getUtilDate("xdatetime");
		Assert.assertEquals(Date.class, actual.getClass());
		assertEquals(utilDate, actual);
	}

	@Test
	public void testGetDateForAttributeWithTypeDateTime()
	{
		Object actual = entity.getDate("xdatetime");
		Assert.assertEquals(java.sql.Date.class, actual.getClass());
		assertEquals(new java.sql.Date(utilDate.getTime()), actual);
	}

	@Test
	public void getAttributeNames()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		DataService dataService = mock(DataService.class);

		DefaultEntity entity = new DefaultEntity(entityMeta, dataService);
		List<String> attrNames = Arrays.asList("attr0", "attr1");
		when(entityMeta.getAtomicAttributeNames()).thenReturn(attrNames);
		assertEquals(Lists.newArrayList(entity.getAttributeNames()), attrNames);
	}

	@Test
	public void getEntities()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		AttributeMetaData labelAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("label").getMock();
		when(entityMeta.getLabelAttribute()).thenReturn(labelAttr);
		DataService dataService = mock(DataService.class);

		Entity refEntity = mock(Entity.class);
		Iterable<Entity> entities = new Iterable<Entity>()
		{
			@Override
			public Iterator<Entity> iterator()
			{
				return Arrays.asList(refEntity).iterator();
			}
		};
		DefaultEntity entity = new DefaultEntity(entityMeta, dataService, Collections.singletonMap("attr", entities));
		assertEquals(entity.getEntities("attr"), entities);
	}

	@Test
	public void setStringObject()
	{
		EntityMetaData entityMeta = mock(EntityMetaData.class);
		AttributeMetaData labelAttr = when(mock(AttributeMetaData.class).getName()).thenReturn("label").getMock();
		when(entityMeta.getLabelAttribute()).thenReturn(labelAttr);
		DataService dataService = mock(DataService.class);

		Entity refEntity = mock(Entity.class);
		DefaultEntity entity = new DefaultEntity(entityMeta, dataService);
		entity.set("attr", refEntity);
		assertEquals(entity.getEntity("attr"), refEntity);
	}
}
